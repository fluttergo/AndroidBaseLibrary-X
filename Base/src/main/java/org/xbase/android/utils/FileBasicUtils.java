package org.xbase.android.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import org.xbase.android.log.Logger;
import org.xbase.android.utils.ShellUtils.CommandResult;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class FileBasicUtils {
    // ===========================================================
    // Constants
    // ===========================================================
    private static final Logger   LOG                   = Logger.getLogger(FileBasicUtils.class);

    public static final String    CHANNEL_DEFAULT       = "Default";
    private static final String   APP_SYSTEM            = "LibSystem";
    private static final String   ROOT_DIR_NAME         = "LibBasic";
    private static final String   ROOT_SUB_DIR_NAME     = "p%d_c_%s";
    // public static final String FOLDER_PID_CHANNEL_FORMAT = "%s/%s";
    private static int            sPid                  = 0;
    private static String         sChannel              = CHANNEL_DEFAULT;

    private static final String   MV_COMMAND_FORMAT     = "mv %s %s";

    private static final String   COPY_COMMAND_FORMAT   = "cp -r %s %s";

    private static final String[] MIGRATION_FOLDER_LIST = new String[] { ".Accounts", ".Database", ".SystemV2",
            ".cache", ".res13", ".log"                 };

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

    public static void config(int pPid, String pChannel) {
        sPid = pPid;
        sChannel = pChannel;
    }

    /**
     * 获取根目录文件夹名字，格式为 LibBasic/p%d_c_%s (p为产品ID， c为渠道）
     * 
     * @return 根目录文件夹名字，例如 LibBasic/p13_c_baidu
     */
    public static String getRootDirNameWithPidChannel() {
        return ROOT_DIR_NAME.concat(File.separator).concat(getPidChannelDirName());
    }

    public static String getPidChannelDirName() {
        return String.format(Locale.getDefault(), ROOT_SUB_DIR_NAME, sPid, sChannel);
    }

    /**
     * 判断SD卡是否可用
     * 
     * @return SD卡可用返回true, 否则返回false.
     */
    public static boolean checkSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean ensureDir(String pDirPath) {
        if (!"".equals(pDirPath) && !File.pathSeparator.equals(pDirPath)) {
            File dir = new File(pDirPath);
            if (!dir.exists()) {
                LOG.ii("mkdir:%s", pDirPath);
                dir.mkdirs();
                if (!dir.exists()) {
                    File parentDir = dir.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        ensureDir(parentDir.getPath());
                    }
                    dir.mkdir();
                }
            }
            return dir.exists();
        }
        return false;
    }

    /**
     * 获取LibBasic/p%d_c_%s目录 没有则创建
     * 
     * @param pContext
     * @return
     */
    public static final String getAppSDCardRootPath(Context pContext) {
        String path = getSDCardPathWithSeparator(pContext) + getRootDirNameWithPidChannel();
        ensureDir(path);
        return path;
    }

    /**
     * 获取SD卡路径
     * 
     * @return
     */
    public static final String getSDCardPath(Context pContext) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();

        File root = new File(path);
        if (getTotalSpace(root) > 0) {
            File dir = new File(root, "LibSDcard");
            if (dir.exists()) {
                dir.delete();
            }

            try {
                if (dir.mkdirs() && dir.createNewFile()) {
                    dir.delete();
                    LOG.d("getSDCardPath final path (Environment) " + path);
                    return path;
                }
            } catch (IOException e) {
                LOG.w("sdcard can't write " + e.toString());
            }
        }

        File extPath = pContext.getExternalFilesDir("");
        if (extPath != null && getTotalSpace(extPath) > 0) {
            File dir = new File(root, "LibSDcard");
            if (dir.exists()) {
                dir.delete();
            }

            if (dir.mkdirs()) {
                dir.delete();
                LOG.d("getSDCardPath final path (extPath) " + path);
                return path;
            }
        }

        extPath = pContext.getDir(APP_SYSTEM, Context.MODE_PRIVATE);
        if (extPath != null) {
            LOG.d("getSDCardPath final path (appPath) " + extPath.getPath());
            return extPath.getPath();
        }

        LOG.d("getSDCardPath final path (Final) " + path);
        return path;
    }

    @SuppressLint("NewApi")
    public static long getTotalSpace(File pFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return pFile.getTotalSpace();
        }
        // implements getTotalSpace() in API lower than GINGERBREAD
        else {
            if (!pFile.exists()) {
                return 0;
            } else {
                final StatFs stats = new StatFs(pFile.getPath());
                // Using deprecated method in low API level system,
                // add @SuppressWarnings("description") to suppress the warning
                return (long) stats.getBlockSize() * (long) stats.getBlockCount();
            }
        }
    }

    /**
     * 获取SD卡路径, 以 {@link File#separator} 结尾
     * 
     * @return
     */
    public static final String getSDCardPathWithSeparator(Context pContext) {
        return getSDCardPath(pContext).concat(File.separator);
    }

    /**
     * 土办法，直接从/proc/mounts获取挂载信息
     * 
     * @return
     */
    protected static final List<String> getMountedStorageList() {
        List<String> paths = new ArrayList<String>();
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            List<String> mounts = new ArrayList<String>();
            String line;
            fileInputStream = new FileInputStream(new File("/proc/mounts"));
            inputStreamReader = new InputStreamReader(fileInputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;
                if (line.contains("obb"))
                    continue;
                if (line.contains("tmpfs"))
                    continue;
                if (line.contains("/dev/mapper"))
                    continue;

                // /dev/block/vold 排除了主SDCard
                // 增加了/dev/fuse, 因为最终会进行去重处理，这样不会遗漏media_rw方式挂载的USB设备
                if ((line.contains("fat") || line.contains("fuse") || line.contains("ntfs"))
                    && (line.contains("/dev/block/vold") || (line.contains("/dev/fuse") && !line.contains("emulated") && !line
                            .contains("shell")))) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        mounts.add(columns[1]);
                    }
                }
            }

            paths.addAll(uniquePaths(mounts));

        } catch (Exception e) {
            LOG.w("getMountedStorageList().error " + e.toString());
        } finally {
            close(bufferedReader);
            close(inputStreamReader);
            close(fileInputStream);
        }
        return paths;
    }

    /**
     * 路劲列表去重，去除那些mount point不同，但是实质是相同存储的路劲，默认保留短路劲
     * 
     * @param pPaths
     * @return
     */
    private static final List<String> uniquePaths(List<String> pPaths) {
        List<String> paths = new ArrayList<String>();
        // 按长度升序
        Collections.sort(pPaths, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.length() - b.length();
            }
        });

        // 容错，去除已存在的标志目录
        for (String path : pPaths) {
            File tmp = new File(path, "LibTemp");
            if (tmp.exists()) {
                tmp.delete();
            }
        }

        // 去重
        for (String path : pPaths) {
            File dir = new File(path);
            File tmp = new File(path, "LibTemp");
            if (dir.canWrite()) {
                if (!tmp.exists()) {
                    if (tmp.mkdir()) {
                        paths.add(path);
                    }
                }
            } else if (dir.canRead()) {
                paths.add(path);
            }
        }

        // 删除测试目录
        for (String path : paths) {
            File tmp = new File(path, "LibTemp");
            if (tmp.exists()) {
                tmp.delete();
            }
        }

        return paths;
    }

    /**
     * 获取文件后缀名, 如 ".png" 或 ".jpg".
     * 
     * @param uri
     *            文件URI
     * 
     * @return 包含(".")的后缀名; 无后缀或uri为空则返回""
     * 
     */
    public static final String getExtension(String uri) {
        if (!TextUtils.isEmpty(uri)) {
            int sep = Math.max(uri.lastIndexOf("\\"), uri.lastIndexOf("/"));
            int dot = uri.lastIndexOf(".");
            if (dot >= 0 && sep < dot) {
                return uri.substring(dot);
            }
        }
        return "";
    }

    /**
     * 获取文件后缀名, 如 "png" 或 "jpg".
     * 
     * @param uri
     *            文件URI
     * 
     * @return 不包含(".")的后缀名; 无后缀或uri为空则返回""
     * 
     */
    public static final String getExtensionWithoutDot(String uri) {
        if (!TextUtils.isEmpty(uri)) {
            int sep = Math.max(uri.lastIndexOf("\\"), uri.lastIndexOf("/"));
            int dot = uri.lastIndexOf(".");
            if (dot >= 0 && sep < dot) {
                return uri.substring(dot + 1);
            }
        }
        return "";
    }

    /**
     * 文件是否存在
     * 
     * @param pFilePath
     * @return
     */
    public static final boolean isFileExist(String pFilePath) {
        if (TextUtils.isEmpty(pFilePath)) {
            return false;
        }
        return new File(pFilePath).exists();
    }

    /**
     * 文件是否可读
     * 
     * @param pFilePath
     * @return
     */
    public static final boolean isFileCanRead(String pFilePath) {
        if (TextUtils.isEmpty(pFilePath)) {
            return false;
        }
        File file = new File(pFilePath);
        if (file.exists()) {
            return file.canRead();
        }
        return false;
    }

    /**
     * 文件夹是否可写
     * 
     * @param pFilePath
     *            dir
     * @return
     */
    public static final boolean isDirCanWrite(String pFilePath) {
        if (TextUtils.isEmpty(pFilePath)) {
            return false;
        }
        // 多个线程同时在该目录下下载，会检测很频繁，导致多线程操作同一个文件
        String filename = ".test." + Thread.currentThread().getName();
        File file = new File(pFilePath, filename);
        try {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            file.createNewFile();
            if (file.exists()) {
                file.delete();
                return true;
            }
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    /**
     * 获取文件或文件夹大小
     * 
     * @param pFile
     * @return 文件或文件夹大小（bytes）
     */
    public static final long getSize(File pFile) {
        long size = 0;
        if (pFile.exists()) {
            if (pFile.isDirectory()) {
                for (File file : pFile.listFiles()) {
                    if (file.isFile())
                        size += file.length();
                    else
                        size += getSize(file);
                }
            } else {
                size += pFile.length();
            }
        }
        return size;
    }

    /**
     * 获取压缩文件原大小
     * 
     * @param pFile
     * @return 文件或文件夹大小（bytes）
     */
    public static final long getUncompressSize(InputStream pInputStream) {
        long size = 0;
        if (pInputStream != null) {
            ZipInputStream zipInputStream = null;
            ZipEntry zipEntry;
            try {
                zipInputStream = new ZipInputStream(new BufferedInputStream(pInputStream));
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    size += zipEntry.getSize();
                    zipInputStream.closeEntry();
                }
            } catch (Exception e) {
                LOG.w("zipDirectory.error " + e.toString());
                close(zipInputStream);

            }
        }
        return size;
    }

    /**
     * 获取压缩文件原大小
     * 
     * @param pFile
     * @return 文件或文件夹大小（bytes）
     */
    public static final long getUncompressSize(File pFile) {
        long size = 0;
        if (pFile.exists() && pFile.isFile()) {
            ZipFile zFile = null;
            try {
                zFile = new ZipFile(pFile);
                Enumeration<? extends ZipEntry> e = zFile.entries();
                ZipEntry entry;
                while (e.hasMoreElements()) {
                    entry = e.nextElement();
                    size += entry.getSize();
                }
            } catch (Exception e) {
                LOG.w("getUncompressSize.error " + e.toString());
            } finally {

                if (zFile != null) {
                    try {
                        zFile.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        if (size <= 0) {
            size = getSize(pFile);
        }
        return size;
    }

    /**
     * 获取路径可用空间大小
     * 
     * @param pPath
     * @return 可用空间大小(bytes)
     */
    @SuppressWarnings("deprecation")
    public static final long getUsableSpace(String pPath) {
        try {
            StatFs statfs = new StatFs(pPath);
            long blockSize = statfs.getBlockSize();
            long availableBlock = statfs.getAvailableBlocks();
            return blockSize * availableBlock;
        } catch (Exception e) {
            LOG.w("getUsableSpace.error " + e.toString());
        }
        return 0;
    }

    /**
     * 获取路径总空间大小
     * 
     * @param pPath
     * @return 总空间大小(bytes)
     */
    @SuppressWarnings("deprecation")
    public static final long getTotalSpace(String pPath) {
        try {
            StatFs statfs = new StatFs(pPath);
            long blockSize = statfs.getBlockSize();
            long blockCount = statfs.getBlockCount();
            return blockSize * blockCount;
        } catch (Exception e) {
            LOG.w("getTotalSpace.error " + e.toString());
        }
        return 0;
    }

    // @formatter:off
    /**
     * 转换byte大小为可阅读大小字符串
     * 
     * @param pBytes bytes大小
     * @param pIsSIUnit 是否使用SI标准
     * @return 可阅读的大小字符串, 如：<br/>
     * <table border="0" cellpadding="0" cellspacing="0" style="padding-right:10px;">
     * <tr style="font-weight:bold;"><td>pIsSIUnit</td><td>false</td><td>true</td></tr>
     * <tr><td>0</td><td>0 B</td><td>0 B</td></tr>
     * <tr><td>27</td><td>27 B</td><td>27 B</td></tr>
     * <tr><td>999</td><td>999 B</td><td>999 B</td></tr>
     * <tr><td>1000</td><td>1000 B</td><td>1.00 kB</td></tr>
     * <tr><td>1023</td><td>1023 B</td><td>1.02 kB</td></tr>
     * <tr><td>1024</td><td>1.00 KiB</td><td>1.02 kB</td></tr>
     * <tr><td>1728</td><td>1.69 KiB</td><td>1.73 kB</td></tr>
     * <tr><td>110592</td><td>108.00 KiB</td><td>110.59 kB</td></tr>
     * <tr><td>7077888</td><td>6.75 MiB</td><td>7.08 MB</td></tr>
     * <tr><td>452984832</td><td>432.00 MiB</td><td>452.98 MB</td></tr>
     * <tr><td>28991029248</td><td>27.00 GiB</td><td>28.99 GB</td></tr>
     * <tr><td>1855425871872</td><td>1.69 TiB</td><td>1.86 TB</td></tr>
     * <tr><td>9223372036854775807</td><td>8.00 EiB</td><td>9.22 EB</td></tr>
     * </table>
     */
    // @formatter:on
    public static final String getHumanReadableSize(long pBytes, boolean pIsSIUnit) {
        StringBuilder result = new StringBuilder();
        int unit = pIsSIUnit ? 1000 : 1024;
        if (pBytes < unit) {
            result.append(pBytes).append(" B");
        } else {
            int exp = (int) (Math.log(pBytes) / Math.log(unit));

            // true 为SI标准，使用Gigabyte单位，原本单位需要增加i，入GiB，考虑老百姓的认知，故去掉i
            // String pre = (pIsSIUnit ? "kMGTPE" : "KMGTPE").charAt(exp - 1) +
            // (pIsSIUnit ? "" : "i");
            String pre = (pIsSIUnit ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (pIsSIUnit ? "" : "");
            result.append(String.format(Locale.getDefault(), "%.1f %s", pBytes / Math.pow(unit, exp), pre));
        }
        return result.toString();
    }

    /**
     * 将数据写入到文件中, 文件存在于/data/data/[package-name]/files中
     * 
     * @param pContext
     * @param pData
     *            需要写入的数据
     * @param pFileName
     *            需要写入的文件名，<span style="color:red">不要包含路径</span>
     */
    public static final void writeFile(Context pContext, String pData, String pFileName) {
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(pContext.openFileOutput(pFileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(pData);
        } catch (IOException e) {
            LOG.e("writeFile[data].error " + e.toString());
        } finally {
            close(outputStreamWriter);
        }

    }

    /**
     * 写入文件，文件存在于外部存储中
     * 
     * @param pData
     *            需要写入的数据
     * @param pPath
     *            文件所在的路径
     * @param pFileName
     *            文件名，<span style="color:red">不要包含路径</span>
     * @param pNeedAppend
     *            是否追加写入，true则为追加写入，false为覆盖写入
     * 
     * @return true 为成功写入
     */
    public static final boolean writeFile(String pData, String pPath, String pFileName, boolean pNeedAppend) {
        if (TextUtils.isEmpty(pPath) || TextUtils.isEmpty(pFileName)) {
            return false;
        }
        File dir = new File(pPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return false;
            }
        }
        if (dir.canWrite()) {
            OutputStream outputStream = null;
            BufferedOutputStream bufferedOutputStream = null;
            try {
                File file = new File(pPath, pFileName);
                if (!file.exists() || !file.isFile()) {
                    file.createNewFile();
                }
                outputStream = new FileOutputStream(file, pNeedAppend);
                bufferedOutputStream = new BufferedOutputStream(outputStream);
                bufferedOutputStream.write(pData.getBytes());
                bufferedOutputStream.flush();

                return true;
            } catch (Exception e) {
                LOG.e("writeFile[any].error " + e.toString());
            } finally {
                close(bufferedOutputStream);
                close(outputStream);
            }
        }
        return false;
    }

    /**
     * 读取/data/data/[package-name]/files中的文件内容
     * 
     * @param pContext
     * @param pFileName
     *            文件名，<span style="color:red">不要包含路径</span>
     * @return
     */
    public static final String readFile(Context pContext, String pFileName) {
        String data = "";
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = pContext.openFileInput(pFileName);
            if (inputStream != null) {
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(String.format("%n"));
                    }
                    stringBuilder.append(receiveString);
                }
                data = stringBuilder.toString();
            }
        } catch (Exception e) {
            LOG.e("readFile.error " + e.toString());
        } finally {
            close(inputStream);
        }
        return data;
    }

    /**
     * 读取文件内容
     * 
     * @param pPath
     *            文件所处文件夹
     * @param pFileName
     *            文件名，<span style="color:red">不要包含路径</span>
     * @return
     */
    public static final String readFile(String pPath, String pFileName) {
        File file = new File(pPath, pFileName);
        return readFile(file);
    }

    /**
     * 读取文件内容
     * 
     * @param pFilePath
     *            文件绝对路径
     * @return
     */
    public static final String readFile(String pFilePath) {
        File file = new File(pFilePath);
        return readFile(file);
    }

    /**
     * 读取文件内容
     * 
     * @param pFile
     *            文件File对象
     * @return
     */
    public static final String readFile(File pFile) {
        String data = "";
        if (pFile != null && pFile.exists() && pFile.canRead()) {
            FileInputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            try {
                inputStream = new FileInputStream(pFile);
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(String.format("%n"));
                    }
                    stringBuilder.append(receiveString);
                }
                data = stringBuilder.toString();
            } catch (Exception e) {
                LOG.e("readFile.error " + e.toString());
            } finally {
                close(bufferedReader);
                close(inputStreamReader);
                close(inputStream);
            }
        }
        return data;
    }

    /**
     * 读取assets中文件
     * 
     * @param pContext
     * @param pFileName
     *            文件名，<span style="color:red">不要包含路径</span>
     * @return
     */
    public static final String readAssetsFile(Context pContext, String pFileName) {
        String data = "";
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = pContext.getResources().getAssets().open(pFileName);
            if (inputStream != null) {
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(String.format("%n"));
                    }
                    stringBuilder.append(receiveString);
                }
                data = stringBuilder.toString();
            }
        } catch (Exception e) {
            LOG.e("readFile.error " + e.toString());
        } finally {
            close(inputStream);
        }
        return data;
    }

    /**
     * 创建文件，如果文件已存在则置空
     * 
     * @param pPath
     * @return 创建的文件File对象
     */
    public static final File createFile(String pPath) {
        File file = new File(pPath);
        if (file.exists()) {
            if (!file.delete()) {
                LOG.w("createFile.warning can not create file [" + pPath + "] it is exit and can not delete!");
                return null;
            }
        }
        try {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    return null;
                }
            }
            if (file.createNewFile()) {
                return file;
            }
        } catch (IOException e) {
            LOG.w("createFile.error " + e.toString());
        }
        return null;
    }

    /**
     * 创建文件夹，兼容Android 4.4 及以上版本
     * 
     * @param pStorage
     *            详见{@link FileBasicUtils#getExternalStorageList(Context)}
     * @param pRelativePath
     *            相对于{@link Storage#getPath()} 的相对路径
     * @return 文件夹对象
     */
    @SuppressLint("NewApi")
    public static final File createFolder(Context pContext, Storage pStorage, String pRelativePath) {
        File folder = new File(pStorage.getPath(), pRelativePath);
        if (!folder.exists() || (folder.exists() && !folder.isDirectory())) {
            if (folder.mkdirs()) {
                return folder;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    File[] externalFilesDirs = pContext.getExternalFilesDirs(pRelativePath);
                    for (File externalFilesDir : externalFilesDirs) {
                        if (externalFilesDir != null) {
                            if (externalFilesDir.getAbsolutePath().startsWith(folder.getAbsolutePath())) {
                                return externalFilesDir;
                            }
                        }
                    }
                }
            }
        } else if (folder.exists() && folder.isDirectory()) {
            return folder;
        }
        return null;
    }
    /**
     * 创建文件夹, 不需要兼容API 19及Android 4.4以上版本
     * 
     * @param pPath
     * @return 文件夹对象
     */
    public static final File createFolder(String pPath) {
        File file = new File(pPath);
        if (!file.exists() || (file.exists() && !file.isDirectory())) {
            if (file.mkdirs()) {
                return file;
            }
        } else if (file.exists() && file.isDirectory()) {
            return file;
        }
        return null;
    }

    public static final boolean moveFile(String pFromPath, String pToPath) {
        File toFile = copyFile(pFromPath, pToPath);
        if (toFile.exists()) {
            File oldFile = new File(pFromPath);
            oldFile.delete();
        }
        return toFile.exists();
    }

    /**
     * 复制文件
     * 
     * @param pFromPath
     *            复制源
     * @param pToPath
     *            复制目标
     * @return 复制后的文件File对象
     */
    public static final File copyFile(String pFromPath, String pToPath) {
        File fromFile = new File(pFromPath);
        File toFile = createFile(pToPath);
        if (fromFile.exists() && fromFile.canRead() && toFile != null && toFile.exists()) {
            InputStream input = null;
            OutputStream output = null;
            try {
                input = new FileInputStream(fromFile);
                output = new FileOutputStream(toFile);
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buf)) > 0) {
                    output.write(buf, 0, bytesRead);
                }
            } catch (Exception e) {
                LOG.e("copyFile.error " + e.toString());
            } finally {
                close(input);
                close(output);
            }
        }
        return toFile;
    }

    /**
     * 移动整个文件夹内容
     * 
     * @param oldPath
     *            String 原文件路径 如：c:/fqf
     * @param newPath
     *            String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static boolean moveFolder(String oldPath, String newPath) {
        boolean moveSucceed = false;
        File oldDir = new File(oldPath);
        if (oldDir.exists()) {
            if (copyFolder(oldPath, newPath)) {
                File newDir = new File(newPath);
                newDir.delete();
                moveSucceed = true;
            }
        }
        return moveSucceed;
    }

    /**
     * 复制整个文件夹内容
     * 
     * @param oldPath
     *            String 原文件路径 如：c:/fqf
     * @param newPath
     *            String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static boolean copyFolder(String oldPath, String newPath) {
        boolean copyAllSucceed = false;
        try {
            (new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
            File oldFile = new File(oldPath);
            String[] file = oldFile.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" + (temp.getName()).toString());
                    byte[] buffer = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(buffer)) != -1) {
                        output.write(buffer, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {// 如果是子文件夹
                    copyAllSucceed = copyAllSucceed && copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                } else {
                    copyAllSucceed = copyAllSucceed && temp.exists();
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
            copyAllSucceed = false;
        }

        return copyAllSucceed;
    }

    /**
     * 删除文件
     * 
     * @param pPath
     * @return true 删除成功；false 其他情况；
     */
    public static final boolean deleteFile(String pPath) {
        File file = new File(pPath);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除目录
     * 
     * @param pRoot
     * @return
     */
    public static final boolean deleteDirectory(File pRoot) {
        if (pRoot.isDirectory()) {
            File[] files = pRoot.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteDirectory(files[i]);
            }
        }
        return pRoot.delete();
    }

    /**
     * 删除目录
     * 
     * @param pRoot
     * @return
     */
    public static final boolean deleteDirectoryFilesByExtention(File pRoot, String pExtention) {
        if (pRoot.isDirectory()) {
            File[] files = pRoot.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isFile()) {
                    if (file.getPath().endsWith(pExtention)) {
                        file.delete();
                    }
                } else {
                    deleteDirectoryFilesByExtention(file, pExtention);
                }
            }
        }
        return true;
    }

    /**
     * 通用close
     * 
     * @param closeable
     */
    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            LOG.w("close.error " + e.toString());
        }
    }

    /**
     * 将Assets文件夹中的文件拷贝到data文件下
     * 
     * @param pContext
     * @param pFileName
     * @return
     */
    public static String copyAssetsFileToData(Context pContext, String pFileName) {
        String path = pContext.getFilesDir().getAbsolutePath() + "/" + pFileName;
        File file = new File(path);
        if (file.exists()) {
            path = file.getAbsolutePath();
        }
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = pContext.getAssets().open(pFileName);
            out = new FileOutputStream(file);
            int length = -1;
            byte[] buf = new byte[1024];
            while ((length = in.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            path = null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return path;
    }

    public static void migrateOldRootDirs(Context pContext, OnMigrationCompleted pCallback) {

        if (checkSDCard()) {
            String oldSdcardPath = getSDCardPathWithSeparator(pContext) + "LibBasic";
            String newDirPath = getAppSDCardRootPath(pContext);
            File oldDir = new File(oldSdcardPath);
            File newDir = new File(newDirPath);
            if (oldDir.exists() && newDir.exists()) {
                CopyFilesTask cpTask = new CopyFilesTask(oldSdcardPath, newDirPath, MIGRATION_FOLDER_LIST);
                cpTask.setOnMigrationCompleted(pCallback);
                cpTask.execute();
            } else {
                LOG.w("Old or new LibBasic folder does not exits!");
            }
        }
    }

    @SuppressWarnings("unused")
    private static void moveOldRootDirs(Context pContext) {
        if (checkSDCard()) {
            String oldSdcardPath = getSDCardPathWithSeparator(pContext) + "LibBasic";
            String newDirPath = getAppSDCardRootPath(pContext);
            File oldDir = new File(oldSdcardPath);
            File newDir = new File(newDirPath);
            if (oldDir.exists() && newDir.exists()) {
                MoveFilesRunnale movement =
                    new MoveFilesRunnale(oldSdcardPath, newDirPath, new MigrationOldFileFilter());
                Thread thread = new Thread(movement);
                thread.setName("moveOldRootDirs");
                thread.start();
            } else {
                LOG.w("Old or new LibBasic folder does not exits!");
            }
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    /**
     * 
     * @author stanley
     * @Create at 2015-10-28 下午3:28:23
     * @Version 1.0
     *          <p>
     *          <strong>将pOldDirPath目录下的所有文件， 移动到新目录：pNewDirPath</strong>
     *          </p>
     */
    private static class MoveFilesRunnale implements Runnable {

        private String     mOldDirPath;
        private String     mNewDirPath;
        private FileFilter mIgnoreFileFilter;

        public MoveFilesRunnale(String pOldDirPath, String pNewDirPath, FileFilter pIgnoreFileFilter) {
            mOldDirPath = pOldDirPath;
            mNewDirPath = pNewDirPath;
            mIgnoreFileFilter = pIgnoreFileFilter;
        }

        @Override
        public void run() {
            File oldDir = new File(mOldDirPath);
            if (oldDir.exists()) {
                File[] subFiles = oldDir.listFiles(mIgnoreFileFilter);
                if (subFiles != null && subFiles.length > 1) { // 要多于一个子目录，
                                                               // 才要迁移（除了目标目录）
                    LOG.i("move files and dirs start...");
                    try {
                        for (File file : subFiles) {
                            LOG.dd("Found old file/foler: ", file.getName());
                            if (!file.getName().equals(getPidChannelDirName())) { // 不能是新的目标目录本身，要不然会递归死循环
                                String newFilePath = mNewDirPath + "/" + file.getName();

                                String mvCmd = String.format(MV_COMMAND_FORMAT, file.getPath(), newFilePath);
                                CommandResult cr = ShellUtils.execCommand(mvCmd, false);
                                if (cr.result != 0) {
                                    LOG.ee("%s failed, Errmsg: %s", mvCmd, cr.errorMsg);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.e(e);
                    }

                    LOG.i("move files and dirs finished.");
                }
            }
        }

    }

    private static class MigrationOldFileFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            Pattern p = Pattern.compile("p\\d+[_]c[_]\\w+");
            Matcher mather = p.matcher(pathname.getName());
            return !mather.matches();
        }

    }

    /**
     * 
     * @author stanley
     * @Create at 2015-10-28 下午3:28:23
     * @Version 1.0
     *          <p>
     *          <strong>将pOldDirPath目录下的所有文件， 移动到新目录：pNewDirPath</strong>
     *          </p>
     */
    private static class CopyFilesTask extends AsyncTask<Void, Void, Void> {

        private String               mOldDirPath;
        private String               mNewDirPath;
        private String[]             mCopyFolders;
        private boolean              mAllCopyCmdSuccess = true;
        private StringBuilder        mErrMsgBuilder     = new StringBuilder();
        private OnMigrationCompleted mOnMigrationCompleted;

        public CopyFilesTask(String pOldDirPath, String pNewDirPath, String[] pCopyFolders) {
            mOldDirPath = pOldDirPath;
            mNewDirPath = pNewDirPath;
            mCopyFolders = pCopyFolders;
        }

        public void setOnMigrationCompleted(OnMigrationCompleted pCallback) {
            mOnMigrationCompleted = pCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            File oldDir = new File(mOldDirPath);
            if (oldDir.exists()) {
                for (String subFolder : mCopyFolders) {
                    File dir = new File(oldDir, subFolder);
                    if (dir.exists()) {
                        String newFilePath = mNewDirPath + "/" + subFolder;
                        String cpCmd = String.format(COPY_COMMAND_FORMAT, dir.getPath(), newFilePath);
                        CommandResult cr = ShellUtils.execCommand(cpCmd, false);
                        mAllCopyCmdSuccess &= (cr.result == 0);
                        if (cr.result != 0) {
                            String errorMsg = String.format("%s failed, Errmsg: %s", cpCmd, cr.errorMsg);
                            LOG.e(errorMsg);
                            mErrMsgBuilder.append(errorMsg);
                            mErrMsgBuilder.append("\n");
                        } else {
                            LOG.ii("Copy dirs:%s to %s finished.", subFolder, newFilePath);
                        }
                    }
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (mOnMigrationCompleted != null) {
                if (mAllCopyCmdSuccess) {
                    mOnMigrationCompleted.onSuccess();
                } else {
                    mOnMigrationCompleted.onFail(-1, mErrMsgBuilder.toString());
                }
            }
        }

    }

    public static interface OnMigrationCompleted {
        public void onSuccess();

        public void onFail(int pErrCode, String pMessage);
    }
}
