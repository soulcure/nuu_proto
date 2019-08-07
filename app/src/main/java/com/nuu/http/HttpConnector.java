package com.nuu.http;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.nuu.config.FileConfig;
import com.nuu.util.AppUtils;
import com.nuu.util.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by colin on 2017/6/6.
 * http utils
 */

public class HttpConnector {

    private static final String TAG = HttpConnector.class.getSimpleName();


    private HttpConnector() {
        throw new AssertionError();
    }

    /**
     * AsyncTask to get data by HttpURLConnection
     * 2016-9-13
     */

    public static void httpGet(String url, IGetListener request) {
        httpGet(null, url, null, request);
    }


    public static void httpGet(ContentValues header, String url,
                               IGetListener request) {
        httpGet(header, url, null, request);
    }

    public static void httpGet(String url, ContentValues params,
                               IGetListener request) {
        httpGet(null, url, params, request);
    }


    public static void httpGet(ContentValues headers, String url,
                               ContentValues params, IGetListener request) {
        HttpGetAsyncTask task = new HttpGetAsyncTask(request);
        task.setParams(params);
        task.setHeaders(headers);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);

    }


    public static void httpPost(ContentValues header, String url,
                                IPostListener request) {
        httpPost(url, null, header, null, request);
    }


    public static void httpPost(String url, ContentValues params,
                                IPostListener request) {
        httpPost(url, params, null, null, request);
    }

    public static void httpPost(ContentValues header, String url,
                                ContentValues params, IPostListener request) {
        httpPost(url, params, header, null, request);
    }


    public static void httpPost(String url, String postBoby,
                                IPostListener request) {
        httpPost(url, null, null, postBoby, request);
    }

    public static void httpPost(String url, ContentValues params,
                                ContentValues headers, String postBoby,
                                IPostListener request) {
        HttpPostAsyncTask task = new HttpPostAsyncTask(request);
        task.setParams(params);
        task.setHeaders(headers);
        task.setPostBoby(postBoby);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }


    /**
     * 多表单混合提交
     *
     * @param url
     * @param params
     * @param files
     * @param request
     */
    public static void httpPostMultipart(String url, ContentValues params,
                                         Map<String, File> files, IPostListener request) {
        httpPostMultipart(url, null, params, files, request);
    }


    /**
     * 多表单混合提交
     *
     * @param url
     * @param headers
     * @param params
     * @param files
     * @param request
     */
    public static void httpPostMultipart(String url, ContentValues headers, ContentValues params,
                                         Map<String, File> files, IPostListener request) {
        HttpPostMultipartAsyncTask task = new HttpPostMultipartAsyncTask(request);
        task.setHearder(headers);
        task.setParams(params);
        task.setFilesMap(files);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }


    private static class HttpGetAsyncTask extends
            AsyncTask<String, Void, String> {

        private IGetListener mRequest;
        private ContentValues mParams;
        private ContentValues mHeaders;

        public HttpGetAsyncTask(IGetListener request) {
            mRequest = request;
        }

        public void setHeaders(ContentValues headers) {
            mHeaders = headers;
        }


        public void setParams(ContentValues headers) {
            mParams = headers;
        }


        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            if (TextUtils.isEmpty(url)) {
                throw new IllegalArgumentException("url is null");
            } else {
                return doGet(mHeaders, url, mParams);
            }
        }

        @Override
        protected void onPostExecute(String response) {
            Log.v(TAG, "httpGet response =" + response);
            if (mRequest != null) {
                mRequest.httpReqResult(response);
            }
        }

    }


    /**
     * 下载文件
     *
     * @param url
     * @param md5
     * @param filePath
     * @param fileName
     * @param listener
     */
    public static void httpDownload(String url, String md5, String filePath,
                                    String fileName, DownloadListener listener) {

        HttpDownLoadFileAsyncTask task = new HttpDownLoadFileAsyncTask(listener);
        task.setMd5(md5);
        task.setFilePath(filePath);
        task.setFileName(fileName);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }


    private static class HttpPostAsyncTask extends
            AsyncTask<String, Void, String> {

        private IPostListener mRequest;
        private ContentValues mParams;
        private ContentValues mHeaders;
        private String mPostBoby;


        public HttpPostAsyncTask(IPostListener request) {
            mRequest = request;
        }

        public void setParams(ContentValues headers) {
            mParams = headers;
        }


        public void setHeaders(ContentValues headers) {
            mHeaders = headers;
        }

        public void setPostBoby(String postBoby) {
            this.mPostBoby = postBoby;
        }

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            if (TextUtils.isEmpty(url)) {
                throw new IllegalArgumentException("url is null");
            }

            if (!TextUtils.isEmpty(mPostBoby)) {
                return doPost(url, mHeaders, mPostBoby);
            } else {
                return doPost(url, mHeaders, mParams);
            }

        }

        @Override
        protected void onPostExecute(String response) {
            Log.v(TAG, "http post  response:" + response);
            if (mRequest != null) {
                mRequest.httpReqResult(response);
            }
        }
    }


    /**
     * 表单提交
     */
    private static class HttpPostMultipartAsyncTask extends
            AsyncTask<String, Void, String> {

        private IPostListener mRequest;

        private ContentValues mParams;
        private ContentValues mHearder;
        private Map<String, File> mFilesMap;

        private HttpPostMultipartAsyncTask(IPostListener request) {
            mRequest = request;
        }

        private void setParams(ContentValues params) {
            this.mParams = params;
        }

        private void setHearder(ContentValues headers) {
            this.mHearder = headers;
        }

        private void setFilesMap(Map<String, File> files) {
            this.mFilesMap = files;
        }

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            if (TextUtils.isEmpty(url)) {
                throw new IllegalArgumentException("url is null");
            }

            try {
                MultipartUtility multipart = new MultipartUtility(url, mHearder, "UTF-8");

                // add for header field  //暂时未添加到http接口
                //multipart.addHeaderField("User-Agent", "CodeJava");
                //multipart.addHeaderField("Test-Header", "Header-Value");

                if (mParams != null && mParams.size() > 0) {
                    for (Map.Entry<String, Object> entry : mParams.valueSet()) {
                        String key = entry.getKey(); // name
                        String value = entry.getValue().toString(); // value
                        multipart.addFormField(key, value);
                    }
                }

                if (mFilesMap != null && mFilesMap.size() > 0) {
                    for (Map.Entry<String, File> entry : mFilesMap.entrySet()) {
                        String key = entry.getKey(); // name
                        File value = entry.getValue(); // value
                        multipart.addFilePart(key, value);
                    }
                }

                return multipart.finish();

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            }

        }

        @Override
        protected void onPostExecute(String response) {
            Log.v(TAG, "http post multipart response:" + response);
            if (mRequest != null) {
                mRequest.httpReqResult(response);
            }
        }


    }


    /**
     * 下载文件
     */
    private static class HttpDownLoadFileAsyncTask extends
            AsyncTask<String, Integer, String> {

        private DownloadListener mProgressListener;
        private String md5;//下载文件的md5
        private String filePath;
        private String fileName;

        private HttpDownLoadFileAsyncTask(DownloadListener listener) {
            mProgressListener = listener;
        }

        private void setMd5(String md5) {
            this.md5 = md5;
        }

        private void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        private void setFileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        protected String doInBackground(String... params) {
            String httpUrl = params[0];

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            if (TextUtils.isEmpty(httpUrl)) {
                throw new IllegalArgumentException("httpUrl is null");
            }

            Log.v(TAG, "http download url:" + httpUrl);

            if (TextUtils.isEmpty(fileName)) {
                fileName = AppUtils.md5(httpUrl);
            }
            if (TextUtils.isEmpty(filePath)) {
                filePath = FileConfig.getFileDownLoadPath();
            }
            final File file = new File(filePath, fileName);
            File tmpFile;
            if (file.exists()) {
                return file.getPath();
            } else {
                // 设置下载的临时文件名
                tmpFile = new File(filePath, fileName + ".tmp");
            }
            long startPosition = tmpFile.length(); // 已下载的文件长度
            String start = "bytes=" + startPosition + "-";
            try {
                URL url = new URL(httpUrl);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("RANGE", start); //支持断点续传
                connection.connect();

                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(tmpFile, true);

                byte[] data = new byte[1024 * 2];
                int total = 0;
                int count;

                final int responseCode = connection.getResponseCode();

                Log.v(TAG, "http download responseCode:" + responseCode);

                //后台不支持断点下载, HTTP_OK
                //后台支持断点下载, HTTP_PARTIAL
                if (responseCode == HttpURLConnection.HTTP_OK ||
                        responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            input.close();
                            return null;
                        }
                        total += count;
                        // publishing the progress....
                        publishProgress(total, fileLength);

                        output.write(data, 0, count);
                    }
                }
            } catch (Exception e) {
                mProgressListener.onFail(e.toString());
                if (tmpFile.exists()) {
                    tmpFile.delete();
                }
                return null;
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                    if (connection != null)
                        connection.disconnect();
                } catch (IOException e) {
                    Log.v(TAG, e.toString());
                }

            }
            if (!TextUtils.isEmpty(md5)) {
                // 下载文件MD5检测
                if (AppUtils.checkFileMd5(tmpFile, md5)) {
                    Log.v(TAG, "download file md5 check success");
                    tmpFile.renameTo(file);
                } else {
                    Log.e(TAG, "download file md5 check fail");
                    tmpFile.delete();
                    return null;

                }
            } else {
                Log.v(TAG, "download file md5 check success");
                tmpFile.renameTo(file);
            }

            return file.getPath();
        }


        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            int cur = progress[0].intValue();
            int total = progress[1].intValue();

            mProgressListener.onProgress(cur, total);

        }


        @Override
        protected void onPostExecute(String strPath) {
            if (strPath != null) {
                mProgressListener.onSuccess(strPath);
            }
        }


    }

    /**
     * http get
     *
     * @param headers
     * @param path
     * @param urlParams
     * @return
     */
    public static String doGet(ContentValues headers, String path, ContentValues urlParams) {
        String response = null;
        HttpURLConnection connection;

        try {
            if (urlParams != null && urlParams.size() > 0) {
                path = path + "?" + getParams(urlParams, true);
            }

            Log.v(TAG, "httpGet url =" + path);
            URL url = new URL(path);

            connection = (HttpURLConnection) url
                    .openConnection();

            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, Object> entry : headers.valueSet()) {
                    String key = entry.getKey(); // name
                    String value = entry.getValue().toString(); // value
                    connection.setRequestProperty(key, value);
                }
            }

            int code = connection.getResponseCode();
            if (code >= 200 && code < 300) {
                response = StreamUtils.readStream(connection.getInputStream());
            } else {
                response = StreamUtils.readStream(connection.getErrorStream());
            }
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            response = e.toString();
        }

        return response;
    }


    /**
     * @param path    http host
     * @param params  http url携带的参数
     * @param headers http请求头信息
     * @return
     */


    public static String doPost(String path, ContentValues headers, ContentValues params) {
        String response = null;
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setConnectTimeout(30000); // 链接超时
            connection.setReadTimeout(20000); // 读取超时

            connection.setDoOutput(true); // 是否输入参数
            connection.setDoInput(true); // 是否向HttpUrLConnection读入，默认true.
            connection.setRequestMethod("POST"); // 提交模式
            connection.setUseCaches(false); // 不能缓存
            connection.setInstanceFollowRedirects(true); // 连接是否尊重重定向
            //connection.setRequestProperty("Content-Type", "application/json"); // 设定传入内容是可序列化的java对象
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, Object> entry : headers.valueSet()) {
                    String key = entry.getKey(); // name
                    String value = entry.getValue().toString(); // value
                    connection.setRequestProperty(key, value);
                }
            }

            connection.connect(); // 连接，所有connection的配置信息必须在connect()方法前提交。

            OutputStream os = connection.getOutputStream();

            if (params != null && params.size() > 0) {
                String postBoby = getParams(params, false);
                if (!TextUtils.isEmpty(postBoby)) {
                    byte[] content = postBoby.getBytes(Charset.forName("UTF-8"));
                    os.write(content, 0, content.length);
                }
            }

            os.flush();
            os.close();

            //服务器修改，错误类型封装到了 response json code里面，此处取消 http ResponseCode 判断
            int code = connection.getResponseCode();
            if (code >= 200 && code < 300) {
                response = StreamUtils.readStream(connection.getInputStream());
            } else {
                response = StreamUtils.readStream(connection.getErrorStream());
            }

            Log.v(TAG, path + " : post response = " + response);
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            response = e.toString();
        }
        return response;
    }


    /**
     * @param path     http host
     * @param headers  http请求头信息
     * @param postBoby http内容
     * @return
     */


    public static String doPost(String path, ContentValues headers, String postBoby) {
        String response = null;
        try {

            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setConnectTimeout(30000); // 链接超时
            connection.setReadTimeout(20000); // 读取超时

            connection.setDoOutput(true); // 是否输入参数
            connection.setDoInput(true); // 是否向HttpUrLConnection读入，默认true.
            connection.setRequestMethod("POST"); // 提交模式
            connection.setUseCaches(false); // 不能缓存
            connection.setInstanceFollowRedirects(true); // 连接是否尊重重定向
            connection.setRequestProperty("Content-Type", "application/json"); // 设定传入内容是可序列化的java对象
            //connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, Object> entry : headers.valueSet()) {
                    String key = entry.getKey(); // name
                    String value = entry.getValue().toString(); // value
                    connection.setRequestProperty(key, value);
                }
            }

            connection.connect(); // 连接，所有connection的配置信息必须在connect()方法前提交。

            OutputStream os = connection.getOutputStream();

            if (!TextUtils.isEmpty(postBoby)) {
                byte[] content = postBoby.getBytes(Charset.forName("UTF-8"));
                os.write(content, 0, content.length);
            }
            os.flush();
            os.close();


            //服务器修改，错误类型封装到了 response json code里面，此处取消 http ResponseCode 判断
            int code = connection.getResponseCode();
            if (code >= 200 && code < 300) {
                response = StreamUtils.readStream(connection.getInputStream());
            } else {
                response = StreamUtils.readStream(connection.getErrorStream());
            }

            Log.v(TAG, path + " : post response = " + response);
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            response = e.toString();
        }
        return response;
    }


    /**
     * 组装参数
     *
     * @param params
     * @param isEncoder
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String getParams(ContentValues params, boolean isEncoder) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, Object> entry : params.valueSet()) {
            String key = entry.getKey(); // name
            String value = entry.getValue().toString(); // value
            if (first) {
                first = false;
                //sb.append("?");
            } else {
                sb.append("&");
            }

            if (isEncoder) {
                sb.append(URLEncoder.encode(key, "UTF-8"));
            } else {
                sb.append(key);
            }

            sb.append("=");

            if (isEncoder) {
                sb.append(URLEncoder.encode(value, "UTF-8"));
            } else {
                sb.append(value);
            }
        }

        return sb.toString();
    }

    /**
     * 组装参数
     *
     * @param params
     * @param isEncoder
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String getParams(Map<String, String> params, boolean isEncoder) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey(); // name
            String value = entry.getValue(); // value
            if (first) {
                first = false;
                //sb.append("?");
            } else {
                sb.append("&");
            }

            if (isEncoder) {
                sb.append(URLEncoder.encode(key, "UTF-8"));
            } else {
                sb.append(key);
            }

            sb.append("=");

            if (isEncoder) {
                sb.append(URLEncoder.encode(value, "UTF-8"));
            } else {
                sb.append(value);
            }
        }

        return sb.toString();
    }

    public static Response httpPost1(String path, Map<String, String> headers,
                                    Map<String, String> params,
                                    String postBoby) {

        Response response = new Response();
        try {

            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setConnectTimeout(30000); // 链接超时
            connection.setReadTimeout(20000); // 读取超时

            connection.setDoOutput(true); // 是否输入参数
            connection.setDoInput(true); // 是否向HttpUrLConnection读入，默认true.
            connection.setRequestMethod("POST"); // 提交模式
            connection.setUseCaches(false); // 不能缓存
            connection.setInstanceFollowRedirects(true); // 连接是否尊重重定向
            connection.setRequestProperty("Content-Type", "application/json"); // 设定传入内容是可序列化的java对象
            //connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    String key = entry.getKey(); // name
                    String value = entry.getValue(); // value
                    connection.setRequestProperty(key, value);
                }
            }

            connection.connect(); // 连接，所有connection的配置信息必须在connect()方法前提交。

            OutputStream os = connection.getOutputStream();

            if (!TextUtils.isEmpty(postBoby)) {
                byte[] content = postBoby.getBytes(Charset.forName("UTF-8"));
                os.write(content, 0, content.length);
            }
            os.flush();
            os.close();


            //服务器修改，错误类型封装到了 response json code里面，此处取消 http ResponseCode 判断
            int code = connection.getResponseCode();
            response.setCode(code);
            response.setContent(StreamUtils.readStream(connection.getInputStream()));

            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            response = null;
        }
        return response;

    }


    public static class Response {
        int code;
        String content;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }


    public static Response httpGet1(String path, Map<String, String> headers,
                                    Map<String, String> params) {
        Response response = new Response();
        try {
            if (params != null && params.size() > 0) {
                path = path + "?" + getParams(params, true);
            }

            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setConnectTimeout(30000); // 链接超时
            connection.setReadTimeout(20000); // 读取超时

            connection.setDoOutput(true); // 是否输入参数
            connection.setDoInput(true); // 是否向HttpUrLConnection读入，默认true.
            connection.setRequestMethod("POST"); // 提交模式
            connection.setUseCaches(false); // 不能缓存
            connection.setInstanceFollowRedirects(true); // 连接是否尊重重定向
            //connection.setRequestProperty("Content-Type", "application/json"); // 设定传入内容是可序列化的java对象
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    String key = entry.getKey(); // name
                    String value = entry.getValue(); // value
                    connection.setRequestProperty(key, value);
                }
            }

            connection.connect(); // 连接，所有connection的配置信息必须在connect()方法前提交。

            OutputStream os = connection.getOutputStream();

            if (params != null && params.size() > 0) {
                String postBoby = getParams(params, false);
                if (!TextUtils.isEmpty(postBoby)) {
                    byte[] content = postBoby.getBytes(Charset.forName("UTF-8"));
                    os.write(content, 0, content.length);
                }
            }

            os.flush();
            os.close();

            //服务器修改，错误类型封装到了 response json code里面，此处取消 http ResponseCode 判断
            int code = connection.getResponseCode();
            response.setCode(code);
            response.setContent(StreamUtils.readStream(connection.getInputStream()));

            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            response = null;
        }

        return response;

    }
}



