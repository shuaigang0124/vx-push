package com.gsg.wx.utils;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO SFTP工具类
 *
 * @author shuaigang
 * @date 2022/9/21 20:14
 */
@Slf4j
public class SftpUtils {

    private String host;//服务器连接ip
    private String username;//用户名
    private String password;//密码
    private int port;//端口号
    private ChannelSftp sftp = null;
    private Session sshSession = null;

    public SftpUtils() {
    }

    public SftpUtils(String host, int port, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    public SftpUtils(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    /**
     * 通过SFTP连接服务器
     */
    public void ftpLogin() {
        try {
            JSch jsch = new JSch();
            jsch.getSession(username, host, port);
            sshSession = jsch.getSession(username, host, port);
            log.info("Session created.");

            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            log.info("Session connected.");

            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            log.info("Opening Channel.");

            sftp = (ChannelSftp) channel;
            log.info("Connected to " + host + ".");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接
     */
    public void ftpLogOut() {
        if (this.sftp != null) {
            if (this.sftp.isConnected()) {
                this.sftp.disconnect();
                log.info("sftp is closed already");
            }
        }
        if (this.sshSession != null) {
            if (this.sshSession.isConnected()) {
                this.sshSession.disconnect();
                log.info("sshSession is closed already");
            }
        }
    }

    /**
     * 批量下载文件
     *
     * @param remotePath：远程下载目录
     * @param localPath：本地保存目录
     * @return
     */
    public List<String> downLoadDirectory(String remotePath, String localPath) {
        List<String> filenames = new ArrayList<String>();
        try {
            Vector v = listFiles(remotePath);
            sftp.cd(remotePath);
            if (v.size() > 0) {
                //System.out.println("本次处理文件个数不为零,开始下载...fileSize=" + v.size());
                Iterator it = v.iterator();
                while (it.hasNext()) {
                    LsEntry entry = (LsEntry) it.next();
                    String filename = entry.getFilename();
                    SftpATTRS attrs = entry.getAttrs();
                    if (!attrs.isDir()) {
                        boolean flag = false;
                        String localFileName = localPath + filename;
                        flag = downloadFile(remotePath, filename, localPath, filename);
                        if (flag) {
                            filenames.add(localFileName);
                        }
                    }
                }
            }
            log.info("download file is success:remotePath=" + remotePath
                    + "and localPath=" + localPath + ",file size is"
                    + v.size());
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            // this.disconnect();
        }
        return filenames;
    }

    /**
     * 下载单个文件
     *
     * @param remotePath：远程下载目录(以路径符号结束)
     * @param remoteFileName：下载文件名
     * @param localPath：本地保存目录(以路径符号结束)
     * @param localFileName：保存文件名
     * @return
     */
    public boolean downloadFile(String remotePath, String remoteFileName, String localPath, String localFileName) {
        FileOutputStream fieloutput = null;
        try {
            sftp.cd(remotePath);
            File file = new File(localPath+"/" + localFileName);
            mkdirs(localPath +"/" + localFileName);
            fieloutput = new FileOutputStream(file);
            sftp.get(remotePath +"/"+ remoteFileName, fieloutput);
            log.info("===DownloadFile:" + remoteFileName + " success from sftp.");
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            if (null != fieloutput) {
                try {
                    fieloutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 上传单个文件
     *
     * @param remotePath：远程保存目录
     * @param remoteFileName：保存文件名
     * @param localPath：本地上传目录
     * @param localFileName：上传的文件名
     * @return
     */
    public boolean uploadFile(String remotePath, String remoteFileName, String localPath, String localFileName) {
        FileInputStream in = null;
        try {
            createDir(remotePath);
            File file = new File(localPath + localFileName);
            in = new FileInputStream(file);
            sftp.put(in, remoteFileName);
                log.info("===uploadFile:" + localFileName + " success from sftp.");
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 批量上传文件
     *
     * @param remotePath：远程保存目录
     * @param localPath：本地上传目录(以路径符号结束) //* @param del：上传后是否删除本地文件
     * @return
     */
    public boolean uploadDirectory(String remotePath, String localPath) {
        boolean flag=true;
        try {
            //ftpLogin();
            File file = new File(localPath);
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    flag = uploadFile(remotePath, files[i].getName(), localPath + "/", files[i].getName());
                    if (flag != true) {
                        break;
                    }
                }
            }
            log.info("upload file is success:remotePath=" + remotePath
                    + "and localPath=" + localPath + ",file size is "
                    + files.length);
            return flag;
        } catch (Exception e) {
            e.printStackTrace();
        } /*finally {
            this.ftpLogOut();
        }*/
        return flag;
    }

    /**
     * 删除本地文件
     *
     * @param filePath
     * @return
     */
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }

        if (!file.isFile()) {
            return false;
        }
        boolean rs = file.delete();
        log.info("delete file success from local.");
        return rs;
    }

    /**
     * 创建目录
     *
     * @param createpath
     * @return
     */
    public boolean createDir(String createpath) {
        try {
            if (exists(createpath)) {
                this.sftp.cd(createpath);
                return true;
            }
            String pathArry[] = createpath.split("/");
            StringBuffer filePath = new StringBuffer("/");
            for (String path : pathArry) {
                if (path.equals("")) {
                    continue;
                }
                filePath.append(path + "/");
                if (exists(filePath.toString())) {
                    sftp.cd(filePath.toString());
                } else {
                    // 建立目录
                    sftp.mkdir(filePath.toString());
                    // 进入并设置为当前目录
                    sftp.cd(filePath.toString());
                }

            }
            this.sftp.cd(createpath);
            return true;
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断目录是否存在
     *
     * @param directory
     * @return
     */
    public boolean exists(String directory) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            isDirExistFlag = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }

    /**
     * 删除stfp文件
     *
     * @param directory：要删除文件所在目录
     * @param deleteFile：要删除的文件
     */
    public void deleteSFTP(String directory, String deleteFile) {
        try {
            // sftp.cd(directory);
            sftp.rm(directory + deleteFile);
            log.info("delete file success from sftp.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果目录不存在就创建目录
     *
     * @param path
     */
    public void mkdirs(String path) {
        File f = new File(path);

        String fs = f.getParent();

        f = new File(fs);

        if (!f.exists()) {
            f.mkdirs();
        }
    }
    public boolean existsIndexFile(String pathName,String ext){
        Vector vector = new Vector();
        boolean flag=true;
        //FTPFile[] files={};
        if(pathName.startsWith("/")&&pathName.endsWith("/")){
            // String directory = pathName;
            //更换目录到当前目录
            try {
                sftp.cd(pathName);
                vector = listFiles(pathName);
            } catch (SftpException e) {
                e.printStackTrace();
            }
            Iterator it = vector.iterator();
            while (it.hasNext()) {
                LsEntry entry = (LsEntry) it.next();
                String filename = entry.getFilename();
                if(filename.equals(ext)){
                    return flag;
                }
            }
        }
        flag=false;
        return flag;
    }

    /**
     * 列出目录下的文件
     *
     * @param directory：要列出的目录
     * @return
     * @throws SftpException
     */
    public Vector listFiles(String directory) throws SftpException {
        return sftp.ls(directory);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ChannelSftp getSftp() {
        return sftp;
    }

    public void setSftp(ChannelSftp sftp) {
        this.sftp = sftp;
    }

    /**
     * 测试
     */
//    public static void main(String[] args) {
//        SftpUtils sftp = null;
//        // 本地存放地址
////        String localPath = "D:\\my-ftp-test\\";
//        // Sftp下载路径
////        String sftpPath = "/data/ftpTest/";
////        List<String> filePathList = new ArrayList<String>();
//        try {
//            sftp = new SftpUtils("175.178.9.64", 22,"root", "ShuaiGang19980510...");
//            sftp.ftpLogin();
//            sftp.deleteSFTP("/shuaigang/frontEnd/","jyh222.html");
//            // 下载
////            sftp.downLoadDirectory(sftpPath, localPath);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            sftp.ftpLogOut();
//        }
//    }
}