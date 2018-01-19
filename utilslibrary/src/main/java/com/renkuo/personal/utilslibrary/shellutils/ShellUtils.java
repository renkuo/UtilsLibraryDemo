package com.renkuo.personal.utilslibrary.shellutils;

import android.text.TextUtils;

import com.renkuo.personal.utilslibrary.ioutils.IoUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Create on 2014年1月22日
 *
 */
public final class ShellUtils {

    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";

    public static boolean checkRootPermission() {
        return execCommand("echo root", true, false).result == 0;
    }

    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[]{command}, isRoot, true);
    }

    public static CommandResult execCommand(List<String> commands, boolean isRoot) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{""}), isRoot, true);
    }

    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        return execCommand(commands, isRoot, true);
    }

    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isRoot, isNeedResultMsg);
    }

    public static CommandResult execCommand(List<String> commands, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{""}), isRoot, isNeedResultMsg);
    }

    public static <T> List<T> execCommand(String... cmd) {
        try {
            String line;
            Process process = Runtime.getRuntime().exec(cmd);
            InputStream is = process.getInputStream();
            InputStreamReader iReader = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(iReader);
            List<T> readResult = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!TextUtils.isEmpty(line)) {
                    readResult.add((T) line);
                }
            }
            reader.close();
            iReader.close();
            is.close();
            process.destroy();
            return readResult;
        } catch (Exception e) {
        }
        return null;
    }

    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                int c;
                while ((c = successResult.read()) != -1) {
                    successMsg.append((char) c);
                }
                while ((c = successResult.read()) != -1) {
                    errorMsg.append((char) c);
                }
            }
        } catch (Exception e) {

        } finally {
            IoUtils.close(os, successResult, errorResult, process);
        }
        return new CommandResult(result, String.valueOf(successMsg), String.valueOf(errorMsg));
    }

    public static class CommandResult {

        public int result;
        public String successMsg;
        public String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }

    public static boolean ping(String address) {
        try {
            Process p = Runtime.getRuntime().exec("ping -c 1 -w 100 " + address);
            int status = p.waitFor();
            if (status == 0) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    private ShellUtils() {/*Do not new me*/}
}
