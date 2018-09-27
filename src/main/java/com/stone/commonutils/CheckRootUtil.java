package com.stone.commonutils;

import com.stone.log.Logs;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Ozawa
 * Email: zequn@kuainiugroup.com
 * Date: 2018/8/13
 * Description：检查是否root
 * <p>
 * Note：9/7/18 by sqq
 * <p>
 * 1. 设备：模拟器 S8 Android8.0 Root
 * 测试：true，执行1000次，耗时 1069 ms，平均每次 1 ms。
 * 2. 设备：模拟器 Android5.0 Root
 * 测试：他人，1000次，耗时 530 ms, 平均每次 0.5 ms。
 * 3. 设备：HTC Android6.0 未Root
 * 测试：false，执行1000次，耗时 88941 ms, 即当前方法平均每次 耗时 89 ms，去除方法内部的log输出，耗时 68956 ms, 平均也仍然远大于 16 ms
 * 4. 设备：XiaoMi Note2 Android8.0 未Root
 * 测试：false, 1000次，耗时 5901 ms
 * <p>
 * 分析：
 * 1. Root 设备耗时短，因为在第一次命中当前判定条件时，就是return true，后续逻辑无需再执行；
 * 2. 未Root 设备，每次检测时都需要全部执行当前判定逻辑，耗时比较平均，也是当前方法的执行耗时的最大值
 * 3. 由于面向的用户绝大多数都是未Root的设备，即当前方法的要划归至耗时操作的序列中。
 * <p>
 * 结论：在主线程中慎用，尽量放置子线程中去执行
 */
@SuppressWarnings("SameParameterValue")
public class CheckRootUtil {
    private static final String TAG = "CheckRootUtil";

    public static boolean isDeviceRooted() {
        if (checkDeviceDebuggable()) {
            return true;
        }//check buildTags
        if (checkSuperuserApk()) {
            return true;
        }//Superuser.apk
        if (checkRootPathSU()) {
            return true;
        }//find su in some path
        if (checkRootWhichSU()) {
            return true;
        }//find su use 'which'
        if (checkBusybox()) {
            return true;
        }//find su use 'which'
        //find su use 'which'
        return checkAccessRootData() || checkGetRootAuth();

    }

    /**
     * @return 查看系统是否测试版
     * 我们可以查看发布的系统版本，是test-keys（测试版），还是release-keys（发布版）。
     * 可以先在adb shell中运行下命令查看：
     * <p>
     * root@android:/ # cat /system/build.prop | grep ro.build.tags
     * ro.build.tags=release-keys
     * <p>
     * 这个返回结果“release-keys”，代表此系统是正式发布版。
     * <p>
     * 若是非官方发布版，很可能是完全root的版本，存在使用风险。
     * 可是在实际情况下，我遇到过某些厂家的正式发布版本，也是test-keys，可能大家对这个标识也不是特别注意吧。所以具体是否使用，还要多考虑考虑呢。也许能解决问题，也许会给自己带来些麻烦。
     */
    private static boolean checkDeviceDebuggable() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    /**
     * @return Superuser.apk是一个被广泛使用的用来root安卓设备的软件，所以可以检查这个app是否存在。
     */
    private static boolean checkSuperuserApk() {
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
//                Logs.d(TAG, "/system/app/Superuser.apk exist");
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * @return su是Linux下切换用户的命令，在使用时不带参数，就是切换到超级用户。通常我们获取root权限，就是使用su命令来实现的，所以可以检查这个命令是否存在。
     * 有三个方法来测试su是否存在：
     * 1）检测在常用目录下是否存在su
     * 这个方法是检测常用目录，那么就有可能漏过不常用的目录。
     * 所以就有了第二个方法，直接使用shell下的命令来查找。
     */
    private static boolean checkRootPathSU() {
        File f;
        final String kSuSearchPaths[] = {"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/"};
        try {
            for (String kSuSearchPath : kSuSearchPaths) {
                f = new File(kSuSearchPath + "su");
                if (f.exists()) {
//                    Logs.d(TAG, "find su in : " + kSuSearchPath);
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * @return 使用which命令查看是否存在su
     * which是linux下的一个命令，可以在系统PATH变量指定的路径中搜索某个系统命令的位置并且返回第一个搜索结果。
     * 这里，我们就用它来查找su。
     * <p>
     * 然而，这个方法也存在一个缺陷，就是需要系统中存在which这个命令。我在测试过程中，就遇到有的Android系统中没有这个命令，所以，这也不是一个完全有保障的方法，倒是可以和上一个方法（在常用路径下查找）进行组合，能提升成功率。
     * 这种查找命令的方式，还有一种缺陷，就是可能系统中存在su，但是已经失效的情况。例如，我曾经root过，后来又取消了，就可能出现这种情况：有su这个文件，但是当前设备不是root的。
     */
    private static boolean checkRootWhichSU() {
        String[] strCmd = new String[]{"/system/xbin/which", "su"};
        ArrayList<String> execResult = executeCommand(strCmd);
        //            Logs.d(TAG, "execResult=" + execResult.toString());
//            Logs.d(TAG, "execResult=null");
        return execResult != null;
    }

    private static ArrayList<String> executeCommand(String[] shellCmd) {
        String line;
        ArrayList<String> fullResponse = new ArrayList<>();
        Process localProcess;
        try {
//            Logs.d(TAG, "to shell exec which for find su :");
            localProcess = Runtime.getRuntime().exec(shellCmd);
        } catch (Exception e) {
            return null;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
        try {
            while ((line = in.readLine()) != null) {
//                Logs.d(TAG, "–> Line received: " + line);
                fullResponse.add(line);
            }
        } catch (Exception ignored) {
        }
//        Logs.d(TAG, "–> Full response was: " + fullResponse);
        return fullResponse;
    }

    /**
     * @return 执行su，看能否获取到root权限
     * 由于上面两种查找方法都存在可能查不到的情况，以及有su文件与设备root的差异，所以，有这第三中方法：我们执行这个命令su。这样，系统就会在PATH路径中搜索su，如果找到，就会执行，执行成功后，就是获取到真正的超级权限了。
     * 这种检测su的方法，应该是最靠谱的，不过，也有个问题，就是在已经root的设备上，会弹出提示框，请求给app开启root权限。这个提示不太友好，可能用户会不喜欢。
     * 如果想安静的检测，可以用上两种方法的组合；如果需要尽量安全的检测到，还是执行su吧。
     */
    private static synchronized boolean checkGetRootAuth() {
        Process process = null;
        DataOutputStream os = null;
        try {
//            Logs.d(TAG, "to exec su");
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
//            Logs.d(TAG, "exitValue=" + exitValue);
            return exitValue == 0;
        } catch (Exception e) {
//            Logs.d(TAG, "Unexpected error - Here is what I know: "
//                    + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) os.close();
                if (process != null) process.destroy();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * @return Android是基于Linux系统的，可是在终端Terminal中操作，会发现一些基本的命令都找不到。这是由于Android系统为了安全，将可能带来风险的命令都去掉了，最典型的，例如su，还有find、mount等。对于一个已经获取了超级权限的人来讲，这是很不爽的事情，所以，便要想办法加上自己需要的命令了。一个个添加命令也麻烦，有一个很方便的方法，就是使用被称为“嵌入式Linux中的瑞士军刀”的Busybox。简单的说BusyBox就好像是个大工具箱，它集成压缩了 Linux 的许多工具和命令。
     * 所以若设备root了，很可能Busybox也被安装上了。这样我们运行busybox测试也是一个好的检测方法。
     */
    private static synchronized boolean checkBusybox() {
        try {
            Logs.d(TAG, "to exec busybox df");
            String[] strCmd = new String[]{"busybox", "df"};
            ArrayList<String> execResult = executeCommand(strCmd);
            //                Logs.d(TAG, "execResult=" + execResult.toString());
//                Logs.d(TAG, "execResult=null");
            return execResult != null;
        } catch (Exception e) {
//            Logs.d(TAG, "Unexpected error - Here is what I know: "
//                    + e.getMessage());
            return false;
        }
    }

    private static synchronized boolean checkAccessRootData() {
        try {
//            Logs.d(TAG, "to write /data");
            String fileContent = "test_ok";
//            Boolean writeFlag = writeFile("/data/su_test", fileContent);
//            if (writeFlag) {
//                Logs.d(TAG, "write ok");
//            } else {
//                Logs.d(TAG, "write failed");
//            }

//            Logs.d(TAG, "to read /data");
            String strRead = readFile("/data/su_test");
//            Logs.d(TAG, "strRead=" + strRead);
            return fileContent.equals(strRead);
        } catch (Exception e) {
//            Logs.d(TAG, "Unexpected error - Here is what I know: "
//                    + e.getMessage());
            return false;
        }
    }

    private static Boolean writeFile(String fileName, String message) {
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            byte[] bytes = message.getBytes();
            fout.write(bytes);
            fout.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String readFile(String fileName) {
        File file = new File(fileName);
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            while ((len = fis.read(bytes)) > 0) {
                bos.write(bytes, 0, len);
            }
            String result = new String(bos.toByteArray());
            Logs.d(TAG, result);
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
