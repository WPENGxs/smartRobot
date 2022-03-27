package com.wpeng.smartrobot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Data_socket {
    Socket socket=null;
    InputStream is;
    BufferedReader br;

    FileInputStream fis;
    DataOutputStream dos;
    /**
     *
     * @param ip
     *          服务器的ip
     * @param port
     *          端口号
     * @throws IOException
     */
    public Data_socket(String ip, int port) throws IOException {//创建连接
        socket=new Socket(ip,port);
    }

    /**
     *
     * @return
     *          返回服务器的数据
     * @throws IOException
     */
    public String GetData() throws IOException {
        if(socket!=null) {
            is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            return br.readLine();
        }
        else{
            return null;
        }
    }

    /**
     *
     * @param str
     *              发送到服务器的数据
     * @throws IOException
     */
    public void SentData(String str) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write((str+"\n").getBytes(StandardCharsets.UTF_8));
        // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞
        outputStream.flush();
    }

    /**
     * socket发送文件
     * @param url
     *              文件的路径
     * @throws IOException
     */
    public void sendFile(String url) throws IOException {
        File file=new File(url);
        try {
            fis = new FileInputStream(file);
            //BufferedInputStream bi=new BufferedInputStream(new InputStreamReader(new FileInputStream(file),"GBK"));
            dos = new DataOutputStream(socket.getOutputStream());//client.getOutputStream()返回此套接字的输出流
            //文件名、大小等属性
            dos.write((file.getName()+"\n").getBytes(StandardCharsets.UTF_8));
            dos.flush();
            dos.write((file.length()+"\n").getBytes(StandardCharsets.UTF_8));
            dos.flush();
            // 开始传输文件
            if(GetData().equals("fine")) {
                byte[] bytes = new byte[1024];
                int length = 0;

                while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                    dos.write(bytes, 0, length);
                    dos.flush();
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     *          断开服务器的连接
     * @throws IOException
     */
    public void Close() throws IOException {
        if(is!=null) {
            is.close();
        }
        if(br!=null) {
            br.close();
        }
        if(fis!=null){
            fis.close();
        }
        if(dos!=null){
            dos.close();
        }
        socket.close();

    }
}
