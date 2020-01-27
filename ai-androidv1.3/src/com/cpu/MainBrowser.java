package com.cpu;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.*;
import android.util.Log;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.Gravity;
import android.graphics.Color;
import android.widget.Toolbar.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.*;
import android.content.Context;
import android.content.Intent;
import android.content.ClipboardManager;
import android.graphics.*;
import android.net.Uri;
import com.cpu.init.ShellExecuter;

public class MainBrowser extends Activity {
    private EditText edHasil;
    private EditText edUrl;
    private Button btnSubmit;
    private String surl = "https://sunjangyo12.000webhostapp.com/login.php/";

    public static String outText = "";
    public static final String DEFAULT_URL = "http://localhost:8080";
    public static final int FILE_CHOOSER_RESULT = 0x01;
    private boolean prosesWeb = true;
    private static final String tag = "MainBrowser";
    private static final int DELAY = 1, ADD_DELAY = 2, CLEAR_DELAY = 3;
    private static final long DISPLAY_TIME = 4000L;
    private static final String GET_HTML = "GET_HTML";
    private static int filefbcounter = 0;
    private static String url = "http://free.facebook.com";
    private String[] xsplit;
    private String dataPost = "";
    private String filefbmetode = "";
    private String filefbpath = "";
    private String filefbnama = "";
    private String filefbsize = "";
    private boolean uploadAuto = false;
    private boolean dataUrlError = false;
    private boolean runDecodingInbox = false;
    private boolean runfilefbpost = false;
    private boolean runfilefbupload = false;
    private WebView webView;
    private ProgressBar urlLoading;
    private ImageView favicon;
    private TextView htmlTitle;
    private RelativeLayout webTitlePanel;
    private EditText gotoUrl;
    private boolean isLoading = false;
    private InputMethodManager imm;
    private ValueCallback<Uri> mFileChooserCallback;
    private ClipboardManager clip;
    private ShellExecuter shell;
    private MainAsisten asisten;
    private SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_browser);

        WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());// for access to favicon in WebView
        asisten = new MainAsisten();
        shell = new ShellExecuter();

        settings = getSharedPreferences("Settings", 0);
        clip = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        gotoUrl = (EditText) findViewById(R.id.goto_url);
        urlLoading = (ProgressBar) findViewById(R.id.progressBar_url_loading);
        favicon = (ImageView) findViewById(R.id.favicon);
        htmlTitle = (TextView) findViewById(R.id.html_title);
        webTitlePanel = (RelativeLayout) findViewById(R.id.webTitlePanel);
        webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true); // включаем поддержку JavaScript
        webView.addJavascriptInterface(new MyJavascriptInterface(), GET_HTML);
        //webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        //webView.getSettings().setPluginsEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.setOnTouchListener(new View.OnTouchListener() 
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Log.d(tag, "onClick");
                if (h.hasMessages(DELAY)) {
                    h.sendEmptyMessage(ADD_DELAY);
                    return false;
                }
                webTitlePanel.setVisibility(View.VISIBLE);
                h.sendEmptyMessageDelayed(DELAY, DISPLAY_TIME);
                return false;
            }
        });

        if (!new File(asisten.getPath(this, "root")+"/dataweb.zip").exists())
        {
            String internal = asisten.getPath(this, "root");

            Intent intent = new Intent(this, MainServer.class);
            intent.putExtra("aksi", "estrak");
            intent.putExtra("assets", "ya");
            intent.putExtra("archive", "dataweb.zip");
            intent.putExtra("path", internal);
            startActivity(intent);
        }else {
            webView.loadUrl(url);
        }

        gotoUrl.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent keyEvent) 
            {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    webView.loadUrl(gotoUrl.getText().toString());
                    return true;
                }
                return false;
            }
        });

        try {
            if (getIntent().getStringExtra("upload").equals("text"))
            {
                NativeWebPageTask task = new NativeWebPageTask();
                task.applicationContext = MainBrowser.this;

                String url = "http://10.42.0.1/client.php?main="+clip.getText().toString();
                task.execute(new String[] { url });
                Toast.makeText(this,"Upload text",Toast.LENGTH_LONG).show();
            }
        } catch(Exception e1) {}

        try {
            Intent intent = getIntent();
            if (intent.getAction().equals(Intent.ACTION_VIEW)) {
                 //стартовал из SiteTools
            }
            url = intent.getDataString();
            if (url == null) {
                url = DEFAULT_URL;
            }
        } catch (Exception e) {}

        try {
            if (!getIntent().getStringExtra("filefbpath").equals(""))
            {
                filefbpath = getIntent().getStringExtra("filefbpath");
                filefbnama = getIntent().getStringExtra("filefbnama");
                filefbsize = getIntent().getStringExtra("filefbsize");

                FilefbTask task = new FilefbTask();
                task.applicationContext = MainBrowser.this;
                task.execute(new String[] { filefbpath, filefbnama });
            }
        }catch(Exception e) {}
    }

    public String webkitText(Context context, String wurl) {
        if (prosesWeb) 
        {
            String ok = "";
            WebView webFbWebkit = new WebView(context);

            class MyJavaScriptInterface 
            {
                private String ok = "";
                public MyJavaScriptInterface(String aContentView) {
                    ok = aContentView;
                }
                @SuppressWarnings("unused")
                public void processContent(String out) {
                    outText = out;
                }
            }
            webFbWebkit.getSettings().setJavaScriptEnabled(true);
            webFbWebkit.addJavascriptInterface(new MyJavaScriptInterface(ok), "INTERFACE");
            webFbWebkit.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap fav) {
                    prosesWeb = false;
                }
                @Override
                public void onPageFinished(WebView view, String url) {
                    view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
                    prosesWeb = true;
                }
            });
            webFbWebkit.loadUrl(wurl);
        }
        return outText;
    }

    public void alertNative(String xurl) 
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Native browser");
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT);
        
        input.setLayoutParams(lp);
        //input.setBackgroundColor(Color.YELLOW);
        input.setText(xurl);
        input.setHint("Url page");
        input.setTextColor(Color.BLACK);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("oke", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) 
            {
                NativeWebPageTask task = new NativeWebPageTask();
                task.applicationContext = MainBrowser.this;
                
                task.execute(new String[] { input.getText().toString() });
            }
        });
        alertDialog.show();
    }

    private class NativeWebPageTask extends AsyncTask<String, Void, String> 
    {
        private ProgressDialog dialog;
        protected Context applicationContext;

        @Override
        protected void onPreExecute() {
            this.dialog = ProgressDialog.show(applicationContext, "request Process", "Please Wait...", true);
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(urls[0]);
            try{
                HttpResponse http = client.execute(request);

                try{
                    InputStream in = http.getEntity().getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder str = new StringBuilder();
                    String line = null;
                    while((line = reader.readLine()) != null){
                        str.append(line + "\n");
                    }
                    in.close();
                    response = str.toString();
                }
                catch(Exception ex){
                    response = "Error split";
                }
            }
            catch(Exception ex){
                response= "Failed Connect to server!";
            }
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            this.dialog.cancel();
            alertNative(result);
        }
    }
    private class FilefbTask extends AsyncTask<String, Void, String> 
    {
        private ProgressDialog dialog;
        protected Context applicationContext;

        @Override
        protected void onPreExecute() {
            this.dialog = ProgressDialog.show(applicationContext, "Encoding File", "Please Wait...", true);
        }

        @Override
        protected String doInBackground(String... urls) {
            filefb(urls[0], urls[1]);
            return "load";
        }
        @Override
        protected void onPostExecute(String result) {
            this.dialog.cancel();
            String[] aksi ={"1. Inbox URL", "2. Postingan URL"};
            AlertDialog.Builder builderIndex = new AlertDialog.Builder(MainBrowser.this);
            builderIndex.setTitle("Pilih Metode?");
            builderIndex.setItems(aksi, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0)
                    {
                        runfilefbpost = true;
                        filefbmetode = "inbox";
                        webView.loadUrl(url);
                    }
                    else if (item == 1) 
                    {
                        runfilefbpost = true;
                        filefbmetode = "postingan";
                        webView.loadUrl(url);
                    } 
                }
            });
            builderIndex.create().show();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            view.loadUrl(url);
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap fav) {
            if (imm != null) {
                imm.hideSoftInputFromWindow(gotoUrl.getWindowToken(), 0);
            }
            urlLoading.setVisibility(View.VISIBLE);
            gotoUrl.setText(url);
            htmlTitle.setText(url);
            favicon.setImageBitmap(fav);
            isLoading = true;
            MainBrowser.this.url = url;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            urlLoading.setVisibility(View.GONE);
            isLoading = false;

            if (runDecodingInbox)
            {
                runDecodingInbox = false;
                //webView.loadUrl("javascript:window." + GET_HTML + ".getProses(document.getElementsByTagName('html')[0].innerHTML);");
            }
            if (runfilefbpost)
            {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainBrowser.this);
                alertDialog.setTitle("Wizard");
                alertDialog.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) 
                    {
                        runfilefbpost = false;
                        runfilefbupload = true;
                        webView.loadUrl(MainBrowser.this.url);
                    }
                });

                if (filefbmetode.equals("inbox"))
                {
                    alertDialog.setMessage("Arahkan url pada ID inbox yang dijadikan target penyimpanan\n\nNote: Alert ini akan muncul jika reload page");
                    alertDialog.show();
                }
                else {
                    alertDialog.setMessage("Arahkan url pada Postingan yang baru dibuat\n\nNote: Alert ini akan muncul jika berada dihalaman yg baru dibuat");
                    dataPost = "#FBDRIVE\\r\\rFilename="+filefbnama+"\\r\\rPathname="+filefbpath+"\\r\\rFilesize="+filefbsize+"\\r\\rEngineBuild=Ai-androidv1.3";
                    webView.loadUrl("javascript:document.forms[1].xc_message.value='"+dataPost+"'");

                    xsplit = url.split("story.php");
                    try {
                        if (!xsplit[1].equals(""))
                        {
                            alertDialog.show();
                        }
                    }catch(Exception e) {}
                }
            }
            if (runfilefbupload && !isLoading)
            {
                dataPost = shell.notNexecuter("cat "+filefbpath+"/"+filefbnama+".base64."+filefbcounter+".file");

                xsplit = url.split("error");
                try {
                    url = xsplit[1]+"\n\nJika muncul pesan merah berisi karakter melebihi 8.000 atau yang lain tandanya file lu terlalu besar, jadi terdeteksi SPAM oleh facebook solusinya coba coment menggunakan facebook lite original";
                    dataUrlError = true;
                }
                catch(Exception e){
                    dataUrlError = false;
                }

                if (dataPost.equals("") || dataUrlError)
                {
                    dataUrlError = false;
                    runfilefbupload = false;

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainBrowser.this);
                    alertDialog.setTitle("Wizard");

                    if (dataPost.equals(""))
                        alertDialog.setMessage("Complete\n\nSemua file berhasil diupload");
                    else
                        alertDialog.setMessage(url);

                    alertDialog.setPositiveButton("Hapus cache", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) 
                        {
                            filefb("shell", "rm "+filefbpath+"/*.file");
                            filefb("shell", "rm "+filefbpath+"/*.base64");
                        }
                    });
                    alertDialog.show();
                }
                else {
                    dataPost = "-title-"+filefbnama+"-title--counter-"+filefbcounter+"-counter-content-"+dataPost+"-content-";
                    
                    if (filefbmetode.equals("inbox"))
                    {
                        webView.loadUrl("javascript:document.forms[1].body.value='"+dataPost+"'");
                        webView.loadUrl("javascript:document.forms[1].submit()");

                        xsplit = url.split("send_success");
                        try {
                            if (xsplit[1].equals("&_rdr"))
                            {
                                toastText(MainBrowser.this, "*** PROCESS ***\n\nCounter:"+filefbcounter+"\n\n*********", Color.YELLOW, Gravity.CENTER);
                                filefbcounter++;
                            }
                        }catch(Exception e){}
                    }
                    else
                    {
                        if (uploadAuto)
                        {
                            webView.loadUrl("javascript:document.forms[0].comment_text.value='"+dataPost+"'");
                            webView.loadUrl("javascript:document.forms[0].submit()");

                            toastText(MainBrowser.this, "*** PROCESS ***\n\nCounter:"+filefbcounter+"\n\n*********", Color.YELLOW, Gravity.CENTER);
                            filefbcounter++;
                        }
                        else{
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainBrowser.this);
                            alertDialog.setTitle("Upload = "+filefbcounter);
                            alertDialog.setMessage("Counter ke satu bisanya tidak terkirim otomatis jadi gunakan ini untuk mengirim manual");

                            alertDialog.setPositiveButton("Auto", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) 
                                {
                                    uploadAuto = true;
                                    webView.loadUrl("javascript:document.forms[0].comment_text.value='"+dataPost+"'");
                                    webView.loadUrl("javascript:document.forms[0].submit()");

                                    toastText(MainBrowser.this, "*** PROCESS ***\n\nCounter:"+filefbcounter+"\n\n*********", Color.YELLOW, Gravity.CENTER);
                                    filefbcounter++;
                                }
                            });
                            alertDialog.setNegativeButton("Manual", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) 
                                {
                                    webView.loadUrl("javascript:document.forms[0].comment_text.value='"+dataPost+"'");
                                    webView.loadUrl("javascript:document.forms[0].submit()");

                                    toastText(MainBrowser.this, "*** PROCESS ***\n\nCounter:"+filefbcounter+"\n\n*********", Color.YELLOW, Gravity.CENTER);
                                    filefbcounter++;
                                }
                            });
                            alertDialog.show();
                        }
                    }
                }
            }
        }
    }
    
    public void toastText(Context context, String data, int warna, int letak)
    {
        LinearLayout layout = new LinearLayout(context);
		
        TextView text = new TextView(context);
        text.setText(data);
        text.setTextColor(Color.BLACK);
        text.setTextSize(13);
        text.setGravity(Gravity.BOTTOM);
        layout.addView(text);

        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(letak, 0, 0);
        toast.setView(text);
        toast.setView(layout);

        View toastView = toast.getView();
        toastView.setBackgroundColor(warna);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //super.onActivityResult(requestCode, resultCode, intent);
        //Log.d(tag, "onActivityResult");
        if (requestCode == FILE_CHOOSER_RESULT) {
            if (mFileChooserCallback == null) {
                //Log.d(tag, "callback null");
                return;
            }
            Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
            mFileChooserCallback.onReceiveValue(result);
            mFileChooserCallback = null;
            //Log.d(tag, "callback result: " + result);
        }
    }

    private class MyWebChromeClient extends WebChromeClient 
    {
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            favicon.setImageBitmap(icon);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            htmlTitle.setText(title);
        }

        //@Override
        public void openFileChooser(ValueCallback<Uri> fileChooserCallback, String acceptType, String capture) {
            // Log.d(tag, "openFileChooser with: acceptType = " + acceptType + " capture = " + capture);
            mFileChooserCallback = fileChooserCallback;
            startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT, Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()), MainBrowser.this, MainFileManager.class), FILE_CHOOSER_RESULT);
        }
    }
    private Handler h = new Handler() {
        private long up = 0;

        @Override
        public void handleMessage(android.os.Message message) {
            //Log.d(tag, "handleMessage");
            switch (message.what) {
                case DELAY:
                    if (up != 0) {
                        sendEmptyMessageDelayed(DELAY, DISPLAY_TIME - (System.currentTimeMillis() - up));
                        up = 0;
                        return;
                    }
                    webTitlePanel.setVisibility(View.GONE);
                    break;
                case ADD_DELAY:
                    up = System.currentTimeMillis();
                    break;
                case CLEAR_DELAY:
                    up = 0;
                    removeMessages(DELAY);
                    webTitlePanel.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, 1, "Get HTML").setIcon(R.drawable.ic_menu_html);
        menu.add(Menu.FIRST, 2, 1, "Get TEXT").setIcon(R.drawable.ic_menu_cookies);
        menu.add(Menu.FIRST, 3, 1, "Get COOKIES").setIcon(R.drawable.ic_menu_cookies);
        menu.add(Menu.FIRST, 4, 1, "NATIVE WEB").setIcon(R.drawable.ic_menu_html);
        menu.add(Menu.FIRST, 5, 1, "BRUTE FORM").setIcon(R.drawable.ic_menu_html);
        menu.add(Menu.FIRST, 6, 1, "FILEFB using spam base64").setIcon(R.drawable.ic_menu_html);
        return true;
    }

    private class MyJavascriptInterface 
    {
        public void getHtml(String html) {
            Intent intent = new Intent(Intent.ACTION_VIEW, null, MainBrowser.this, MainEditor.class);
            intent.putExtra(MainEditor.CODE, html);
            intent.putExtra(MainEditor.CODE_TYPE, MainEditor.HTML);
            startActivityForResult(intent, MainEditor.REQUEST_VIEW_SOURCE);
        }
        public void getProses(String text) {
            String[] prev1 = text.split("see_older");
            String[] prev2 = prev1[1].split("refid=12");
            String[] prev3 = prev2[0].split("/messages");

            String prev = "https://free.facebook.com/messages"+prev3[1]+"refid=12";
            webView.loadUrl(prev);

            Log.i("zzzz", prev);

            /*SharedPreferences.Editor editor = settings.edit();
            editor.putString("decodingInbox", prev);
            editor.commit();*/
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        if (item.getItemId() == 1)
        {
            webView.loadUrl("javascript:window." + GET_HTML + ".getHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        }
        if (item.getItemId() == 2)
        {
            webView.loadUrl("javascript:window." + GET_HTML + ".getHtml(document.getElementsByTagName('body')[0].innerText);");
        }
        if (item.getItemId() == 3)
        {
            CookieManager cman = CookieManager.getInstance();
            Intent intent = new Intent(Intent.ACTION_VIEW, null, this, MainEditor.class);
            intent.putExtra(MainEditor.CODE, cman.getCookie(url));
            intent.putExtra(MainEditor.CODE_TYPE, MainEditor.NONE);
            startActivityForResult(intent, MainEditor.REQUEST_VIEW_SOURCE);
        }
        if (item.getItemId() == 4)
        {
            alertNative("http://google.com");
        }
        if (item.getItemId() == 5)
        {
        }
        if (item.getItemId() == 6)
        {
            String[] aksi ={"1. Send File", "2. Receive File"};
            AlertDialog.Builder builderIndex = new AlertDialog.Builder(MainBrowser.this);
            builderIndex.setTitle("Filemanager using facebook");
            builderIndex.setItems(aksi, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0){
                        Intent intent = new Intent(MainBrowser.this, MainFileManager.class);
                        intent.putExtra("aksi", "filefb");
                        MainBrowser.this.startActivity(intent);
                    }
                    else if (item == 1) {
                        String[] aksi ={"1. Inbox DECODING", "2. Postingan DECODING"};
                        AlertDialog.Builder builderIndex = new AlertDialog.Builder(MainBrowser.this);
                        builderIndex.setTitle("Pilih Metode?");
                        builderIndex.setItems(aksi, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == 0)
                                {
                                    //runDecodingInbox = true;
                                    //webView.loadUrl(settings.getString("decodingInbox", ""));
                                    webView.loadUrl("javascript:window." + GET_HTML + ".getProses(document.getElementsByTagName('html')[0].innerHTML);");
                                }
                                else if (item == 1) 
                                {
                                } 
                            }
                        });
                        builderIndex.create().show();
                    } 
                }
            });
            builderIndex.create().show();
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        if (webView.canGoBack()) {
            webView.goBack();
        }
        else {
            finish();
        }
    }

    private native void filefb(String xpath, String xnama);

    static {
        System.loadLibrary("main");
    }
}