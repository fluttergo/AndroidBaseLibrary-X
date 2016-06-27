package org.xbase.android.utils;

import android.content.Context;

import org.xbase.android.log.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class ZipUtils {
    // ===========================================================
    // Constants
    // ===========================================================
    private static final Logger LOG                 = Logger.getLogger(ZipUtils.class);
    private static final int    BUFFER_SIZE         = 8192;
    private static final int    NOTIFY_COUNT        = 125;

    // 解压状态码
    public static final int     STATUS_UNZIP_START  = 1;
    public static final int     STATUS_UNZIPPING    = 2;
    public static final int     STATUS_UNZIP_FINISH = 3;
    public static final int     STATUS_UNZIP_FAIL   = 4;

    // 压缩状态码
    public static final int     STATUS_ZIP_START    = 1;
    public static final int     STATUS_ZIPPING      = 2;
    public static final int     STATUS_ZIP_FINISH   = 3;
    public static final int     STATUS_ZIP_FAIL     = 4;

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * 压缩文件
     * 
     * @param pInputFilePath
     *            需用进行压缩的文件路径
     * @param pOutputFilePath
     *            压缩文件输出路径
     * @param pZipStatusListener
     *            压缩过程监听，详见{@link IZipStatusListener}
     * @return 压缩文件File对象. null为压缩不成功
     */
    public static File zipFile(Context pContext, String pInputFilePath, String pOutputFilePath,
                               IZipStatusListener pZipStatusListener) {
        File input = new File(pInputFilePath);
        if (input.exists() && input.canRead()) {
            ZipOutputStream zipOutputStream = null;
            FileInputStream fileInputStream = null;
            try {
                zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(pOutputFilePath)));
                ZipEntry entry = new ZipEntry(pOutputFilePath);
                zipOutputStream.putNextEntry(entry);
                fileInputStream = new FileInputStream(input);
                LOG.d("zip : " + pInputFilePath);

                long totalBytes = input.length();
                if (pZipStatusListener != null) {
                    pZipStatusListener.onStart(totalBytes);
                }

                int len;
                int readCount = 0;
                long readedBytes = 0;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((len = fileInputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
                    readedBytes += len;
                    readCount++;
                    if (readCount % NOTIFY_COUNT == 0) {
                        if (pZipStatusListener != null) {
                            pZipStatusListener.onProcess(readedBytes);
                        }
                    }
                }
                zipOutputStream.closeEntry();
                if (pZipStatusListener != null) {
                    pZipStatusListener.onFinish();
                }

                return new File(pOutputFilePath);

            } catch (IOException e) {
                LOG.w("zipFile.error " + e.toString());
                if (pZipStatusListener != null) {
                    pZipStatusListener.onFail("Error occurred in zip processing.");
                }
            } finally {
                close(zipOutputStream);
                close(fileInputStream);
            }
        }
        if (pZipStatusListener != null) {
            pZipStatusListener.onFail("Unable to read from input file, zip-process stoped.");
        }
        return null;
    }

    /**
     * 压缩文件夹
     * 
     * @param pInputDirectoryPath
     *            需要压缩的文件夹路径
     * @param pOutputFilePath
     *            压缩文件输出路径
     * @param pZipStatusListener
     *            压缩过程监听，详见{@link IZipStatusListener}
     * @return 压缩文件File对象. null为压缩不成功
     */
    public static File zipFolder(Context pContext, String pInputDirectoryPath, String pOutputFilePath,
                                 IZipStatusListener pZipStatusListener) {
        File input = new File(pInputDirectoryPath);
        if (input.exists() && input.canRead()) {
            ZipOutputStream zos = null;
            byte[] buffer = new byte[BUFFER_SIZE];
            try {
                zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(pOutputFilePath)));

                long totalBytes = FileBasicUtils.getSize(input);
                if (pZipStatusListener != null) {
                    pZipStatusListener.onStart(totalBytes);
                }

                int readCount = 0;
                long readedBytes = 0;
                File[] fileList = input.listFiles();
                for (int i = 0; i < fileList.length; i++) {
                    LOG.d("zip add : " + fileList[i].getAbsolutePath());
                    ZipEntry entry = new ZipEntry(fileList[i].getAbsolutePath());
                    zos.putNextEntry(entry);
                    FileInputStream in = new FileInputStream(fileList[i].getAbsolutePath());
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                        readedBytes += len;
                        readCount++;
                        if (readCount % NOTIFY_COUNT == 0) {
                            if (pZipStatusListener != null) {
                                pZipStatusListener.onProcess(readedBytes);
                            }
                        }
                    }
                    zos.closeEntry();
                    close(in);
                }
                if (pZipStatusListener != null) {
                    pZipStatusListener.onFinish();
                }
                return new File(pOutputFilePath);

            } catch (IOException e) {
                LOG.w("zipDirectory.error " + e.toString());
                if (pZipStatusListener != null) {
                    pZipStatusListener.onFail("Error occurred in zip processing.");
                }
            } finally {
                close(zos);
            }
        }
        if (pZipStatusListener != null) {
            pZipStatusListener.onFail("Unable to read from input file, zip-process stoped.");
        }
        return null;
    }

    /**
     * 解压缩文件
     * 
     * @param pContext
     * @param pZipFilePath
     *            压缩文件路径
     * @param pStorage
     *            详见{@link FileBasicUtils#getExternalStorageList(Context)}
     * @param pOutputRelativePath
     *            解压缩路径(<span style="color:red">相对于{@link Storage#getPath()}
     *            的相对路径</span>)
     * @param pUnZipStatusListener
     *            解压缩过程监听，详见{@link IUnZipStatusListener}
     * @return true 解压成功； false 解压失败
     */
    public static final boolean unzip(Context pContext, String pZipFilePath, Storage pStorage,
                                      String pOutputRelativePath, IUnZipStatusListener pUnZipStatusListener) {
        File outputFolder = FileBasicUtils.createFolder(pContext, pStorage, pOutputRelativePath);
        if (outputFolder != null && outputFolder.canWrite()) {
            return unzip(pContext, pZipFilePath, outputFolder.getPath(), pUnZipStatusListener);
        }
        return false;
    }

    /**
     * 解压缩文件
     * 
     * @param pZipFilePath
     *            压缩文件路径
     * @param pOutputPath
     *            解压缩路径
     * @param pUnZipStatusListener
     *            解压缩过程监听，详见{@link IUnZipStatusListener}
     * @return
     */
    public static final boolean unzip(String pZipFilePath, String pOutputPath, IUnZipStatusListener pUnZipStatusListener) {
        return unzip(null, pZipFilePath, pOutputPath, pUnZipStatusListener);
    }

    /**
     * 解压缩文件
     * 
     * @param pContext
     * @param pZipFilePath
     *            压缩文件路径
     * @param pOutputPath
     *            解压缩路径
     * @param pUnZipStatusListener
     *            解压缩过程监听，详见{@link IUnZipStatusListener}
     * @return
     */
    public static final boolean unzip(Context pContext, String pZipFilePath, String pOutputPath,
                                      IUnZipStatusListener pUnZipStatusListener) {
        ArrayList<String> files = new ArrayList<String>();
        File zipFile = new File(pZipFilePath);
        File outputFolder = new File(pOutputPath);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        if (zipFile.exists() && zipFile.canRead() && outputFolder != null && outputFolder.canWrite()) {
            FileInputStream fileInputStream = null;
            ZipInputStream zipInputStream = null;
            ZipEntry zipEntry;
            byte[] buffer = new byte[BUFFER_SIZE];
            long totalBytes = FileBasicUtils.getUncompressSize(zipFile);
            LOG.d("unzip data " + totalBytes);
            if (pUnZipStatusListener != null) {
                pUnZipStatusListener.onStart(totalBytes);
            }

            try {
                fileInputStream = new FileInputStream(zipFile);
                zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));
                String outputRootPath = outputFolder.getAbsolutePath().concat(File.separator);

                int readCount = 0;
                long readedBytes = 0;
                LOG.d("start unzip");
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String fileName = zipEntry.getName();

                    // 判断剩余空间
                    if (FileBasicUtils.getUsableSpace(outputFolder.getAbsolutePath()) < zipEntry.getSize()) {
                        if (pUnZipStatusListener != null) {
                            pUnZipStatusListener.onFail("Out of disk space, zip-process stoped.");
                        }
                        // 清除已解压的文件
                        for (String file : files) {
                            FileBasicUtils.deleteFile(file);
                        }
                        return false;
                    }
                    File file = new File(outputRootPath, fileName);
                    files.add(file.getPath());
                    if (zipEntry.isDirectory()) {
                        file.mkdirs();
                    } else {
                        File parentF = file.getParentFile();
                        if (parentF != null && !parentF.exists()) {
                            parentF.mkdirs();
                        }
                        if (file.exists()) {
                            file.delete();
                        }
                        FileOutputStream outputStream = new FileOutputStream(outputRootPath + fileName);
                        BufferedOutputStream outputBufStream = new BufferedOutputStream(outputStream, buffer.length);
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            outputBufStream.write(buffer, 0, len);
                            readedBytes += len;
                            readCount++;
                            if (readCount % NOTIFY_COUNT == 0) {
                                if (pUnZipStatusListener != null) {
                                    pUnZipStatusListener.onProcess(readedBytes);
                                }
                            }
                        }
                        outputBufStream.flush();
                        close(outputBufStream);
                        close(outputStream);
                    }
                    zipInputStream.closeEntry();
                }
                LOG.d("unzip finish");
                if (pUnZipStatusListener != null) {
                    pUnZipStatusListener.onFinish(files);
                }
                return true;
            } catch (Exception e) {
                LOG.w("unzip.error " + e.toString());
                // 清除已解压的文件
                for (String file : files) {
                    FileBasicUtils.deleteFile(file);
                }
                if (pUnZipStatusListener != null && pContext != null) {
                    pUnZipStatusListener.onFail("Error occurred in zip processing.");
                }
            } finally {
                close(zipInputStream);
                close(fileInputStream);
            }
        }
        if (pUnZipStatusListener != null && pContext != null) {
            pUnZipStatusListener.onFail("Unable to read from input file, zip-process stoped.");
        }
        return false;
    }

    public static final boolean unzipres(Context pContext, int res, String pOutputPath,
                                         IUnZipStatusListener pUnZipStatusListener) {
        ArrayList<String> files = new ArrayList<String>();
        InputStream pInputStream = pContext.getResources().openRawResource(res);
        File outputFolder = new File(pOutputPath);
        if (pInputStream != null && outputFolder != null && outputFolder.canWrite()) {

            ZipInputStream zipInputStream = null;
            ZipEntry zipEntry;
            byte[] buffer = new byte[BUFFER_SIZE];
            long totalBytes = FileBasicUtils.getUncompressSize(pInputStream);
            if (pUnZipStatusListener != null) {
                pUnZipStatusListener.onStart(totalBytes);
            }
            try {
                pInputStream = pContext.getResources().openRawResource(res);
                zipInputStream = new ZipInputStream(new BufferedInputStream(pInputStream));
                String outputRootPath = outputFolder.getAbsolutePath().concat(File.separator);

                int readCount = 0;
                long readedBytes = 0;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String fileName = zipEntry.getName();
                    LOG.d("unzip : " + zipEntry.getName() + " " + zipEntry.getSize());

                    // 判断剩余空间
                    if (FileBasicUtils.getUsableSpace(outputFolder.getAbsolutePath()) < zipEntry.getSize()) {
                        if (pUnZipStatusListener != null) {
                            pUnZipStatusListener.onFail("Out of disk space, zip-process stoped.");
                        }
                        // 清除已解压的文件
                        for (String file : files) {
                            FileBasicUtils.deleteFile(file);
                        }
                        return false;
                    }
                    File file = new File(outputRootPath, fileName);
                    files.add(file.getPath());
                    if (zipEntry.isDirectory()) {
                        file.mkdirs();
                    } else {
                        File parentF = file.getParentFile();
                        if (parentF != null && !parentF.exists()) {
                            parentF.mkdirs();
                        }
                        if (file.exists()) {
                            file.delete();
                        }
                        FileOutputStream outputStream = new FileOutputStream(outputRootPath + fileName);
                        BufferedOutputStream outputBufStream = new BufferedOutputStream(outputStream, buffer.length);
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            outputBufStream.write(buffer, 0, len);
                            readedBytes += len;
                            readCount++;
                            if (readCount % NOTIFY_COUNT == 0) {
                                if (pUnZipStatusListener != null) {
                                    pUnZipStatusListener.onProcess(readedBytes);
                                }
                            }
                        }
                        outputBufStream.flush();
                        close(outputStream);
                        close(outputBufStream);
                    }
                    zipInputStream.closeEntry();
                }
                if (pUnZipStatusListener != null) {
                    pUnZipStatusListener.onFinish(files);
                }
                return true;
            } catch (Exception e) {
                LOG.w("zipDirectory.error " + e.toString());
                // 清除已解压的文件
                for (String file : files) {
                    FileBasicUtils.deleteFile(file);
                }
                if (pUnZipStatusListener != null) {
                    pUnZipStatusListener.onFail("Error occurred in zip processing.");
                }
            } finally {
                close(zipInputStream);
            }
        }
        if (pUnZipStatusListener != null) {
            pUnZipStatusListener.onFail("Unable to read from input file, zip-process stoped.");
        }
        return false;
    }

    public static boolean unzipAssetsFile(Context pContext, String pOutputPath, String srcName,
                                          IUnZipStatusListener pUnZipStatusListener) {

        ArrayList<String> files = new ArrayList<String>();
        InputStream pInputStream = null;
        try {
            pInputStream = pContext.getAssets().open(srcName);
        } catch (Exception e) {

        }
        File outputFolder = new File(pOutputPath);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        if (pInputStream != null && outputFolder != null && outputFolder.canWrite()) {
            ZipInputStream zipInputStream = null;
            zipInputStream = new ZipInputStream(new BufferedInputStream(pInputStream));
            ZipEntry zipEntry;
            byte[] buffer = new byte[BUFFER_SIZE];
            try {
                String outputRootPath = outputFolder.getAbsolutePath().concat(File.separator);
                int readCount = 0;
                long readedBytes = 0;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String fileName = zipEntry.getName();
                    // 判断剩余空间
                    if (FileBasicUtils.getUsableSpace(outputFolder.getAbsolutePath()) < zipEntry.getSize()) {
                        if (pUnZipStatusListener != null) {
                            pUnZipStatusListener.onFail("Out of disk space, zip-process stoped.");
                        }
                        // 清除已解压的文件
                        for (String file : files) {
                            FileBasicUtils.deleteFile(file);
                        }
                        return false;
                    }
                    File file = new File(outputRootPath, fileName);
                    files.add(file.getPath());
                    if (zipEntry.isDirectory()) {
                        file.mkdirs();
                    } else {
                        File parentF = file.getParentFile();
                        if (parentF != null && !parentF.exists()) {
                            parentF.mkdirs();
                        }
                        if (file.exists()) {
                            file.delete();
                        }
                        FileOutputStream outputStream = new FileOutputStream(outputRootPath + fileName);
                        BufferedOutputStream outputBufStream = new BufferedOutputStream(outputStream, buffer.length);
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            outputBufStream.write(buffer, 0, len);
                            readedBytes += len;
                            readCount++;
                            if (readCount % NOTIFY_COUNT == 0) {
                                if (pUnZipStatusListener != null) {
                                    pUnZipStatusListener.onProcess(readedBytes);
                                }
                            }
                        }
                        outputBufStream.flush();
                        close(outputBufStream);
                        close(outputStream);
                    }
                    zipInputStream.closeEntry();
                }
                if (pUnZipStatusListener != null) {
                    pUnZipStatusListener.onFinish(files);
                }
                return true;
            } catch (Exception e) {
                LOG.w("zipDirectory.error " + e.toString());
                // 清除已解压的文件
                for (String file : files) {
                    FileBasicUtils.deleteFile(file);
                }
                if (pUnZipStatusListener != null) {
                    pUnZipStatusListener.onFail("Error occurred in zip processing.");
                }
            } finally {
                close(zipInputStream);
            }
        }
        if (pUnZipStatusListener != null) {
            pUnZipStatusListener.onFail("Unable to read from input file, zip-process stoped.");
        }
        return false;
    }

    /**
     * 通用close
     * 
     * @param closeable
     */
    private static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            LOG.w("close.error " + e.toString());
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    /**
     * 解压缩状态监听
     * 
     * @author XiXi
     * @Create at 2014-7-1 上午10:49:45
     * @Version 1.0
     *          <p>
     *          <strong>Features draft description.主要功能介绍</strong>
     *          </p>
     */
    public static interface IUnZipStatusListener {
        public void onStart(long pTotalBytes);

        public void onProcess(long pUnZipedBytes);

        public void onFinish(ArrayList<String> pPaths);

        public void onFail(String pMessage);
    }

    /**
     * 压缩状态监听
     * 
     * @author XiXi
     * @Create at 2014-7-1 上午10:50:04
     * @Version 1.0
     *          <p>
     *          <strong>Features draft description.主要功能介绍</strong>
     *          </p>
     */
    public static interface IZipStatusListener {
        public void onStart(long pTotalBytes);

        public void onProcess(long pZipedBytes);

        public void onFinish();

        public void onFail(String pMessage);
    }
}
