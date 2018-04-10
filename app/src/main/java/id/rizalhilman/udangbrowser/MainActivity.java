package id.rizalhilman.udangbrowser;

import android.content.DialogInterface;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import id.rizalhilman.udangbrowser.helper.MyWebViewClient;

public class MainActivity extends AppCompatActivity {

    // TODO 1 : Dekalarsi variable
    private ImageButton btnBack, btnForward;
    private ImageButton btnGo, btnMenu;
    private WebView wvMainWeb;
    private EditText etUrlTujuan;
    private ProgressBar progresWeb;
    private String URL_WEB = "http://www.google.com";

    private InputMethodManager inputManager;
    private PopupMenu mainMenu;
    private SwipeRefreshLayout swipeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // inisialisasi widget
        initComponent();
        eventListener();
        mainFunction();
    }

    private void initComponent() {
        btnBack = findViewById(R.id.btnBack);
        btnForward = findViewById(R.id.btnForward);
        btnGo = findViewById(R.id.btnGo);
        btnMenu = findViewById(R.id.btnMenu);
        etUrlTujuan = findViewById(R.id.etUrlTujuan);
        wvMainWeb = findViewById(R.id.wvMainWeb);
        progresWeb = findViewById(R.id.progresWeb);
        inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mainMenu = new PopupMenu(this, btnMenu);
        // inflate popup menu menggunakan xml file
        mainMenu.getMenuInflater().inflate(R.menu.main_menu, mainMenu.getMenu());

        swipeLayout = findViewById(R.id.swipeLayout);
    }
    private void eventListener() {
        // menangani klik di btnGo
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // dapatkan URL baru dari edit text
                URL_WEB = etUrlTujuan.getText().toString();
                // Load web view
                wvMainWeb.loadUrl(URL_WEB);
            }
        });
        // Menangai klik back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // kembali ke halaman sebelumnya
                wvMainWeb.goBack();
            }
        });
        // Menangai klik forward
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // kembali ke halaman setelahnya
                wvMainWeb.goForward();
            }
        });
        // ketika tombol enter diklik
        etUrlTujuan.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO){
                    // dapatkan URL baru dari edit text
                    URL_WEB = etUrlTujuan.getText().toString();
                    // Load web view
                    wvMainWeb.loadUrl(URL_WEB);
                }
                return handled;
            }
        });
        // ketik btn menu di klik
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // munculkan popun menu
                mainMenu.show();
            }
        });
        // Menghandle event popup menu yg dipilih
        mainMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuId = item.getItemId();
                switch (menuId){
                    case R.id.nav_about:
                        // munculkan alert about
                        aboutApp();
                        break;
//                    case R.id.nav_setting:
////                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
//                        break;
                    case R.id.nav_exit:
                        // munculkan confirm alert
                        confirmExit();
                        break;
                }
                return true;
            }
        });

        // swipe refresh listener
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // load lagi web viewnya
                wvMainWeb.loadUrl(URL_WEB);
            }
        });
    }

    private void aboutApp() {
        AlertDialog.Builder alertConfirm = new AlertDialog.Builder(this);
        //alertConfirm.setTitle("About Udang Browser");
        View viewAbout = getLayoutInflater().inflate(R.layout.about_udang, null);
        alertConfirm.setView(viewAbout);
        alertConfirm.show();
    }

    private void confirmExit() {
        AlertDialog.Builder alertConfirm = new AlertDialog.Builder(this);
        alertConfirm.setTitle("Confirmation");
        alertConfirm.setMessage("Exit Udang Browser ?");
        alertConfirm.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // exit sytem
                System.exit(0);
            }
        });
        alertConfirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // biarin aja nanti close sendiri
            }
        });
        alertConfirm.show();
    }

    private void mainFunction() {
        wvMainWeb.getSettings().setJavaScriptEnabled(true);
        wvMainWeb.getSettings().setDatabaseEnabled(true);
        wvMainWeb.getSettings().setAppCacheEnabled(true);
        wvMainWeb.getSettings().setAllowFileAccess(true);

        wvMainWeb.setWebViewClient(new MyWebViewClient());
        // set web chrome clinet
        wvMainWeb.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progresWeb.setProgress(newProgress);
                if (newProgress == 100){
                    progresWeb.setVisibility(View.INVISIBLE);
                    // cek kemungkinan swipe layout sedang merefresh
                    if (swipeLayout.isRefreshing()){
                        // hentikan
                        swipeLayout.setRefreshing(false);
                    }
                } else {
                    // set URL baru ke edit text
                    URL_WEB = wvMainWeb.getUrl();
                    etUrlTujuan.setText(URL_WEB);
                    // munculkan proggres bar
                    progresWeb.setVisibility(View.VISIBLE);
                    progresWeb.setClickable(false);

                    // sembunyikan soft keyboard
                    if (inputManager.isActive()){
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    }
                }
            }

        });
        // Tampilkan website
        wvMainWeb.loadUrl(URL_WEB);
        wvMainWeb.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (wvMainWeb != null){
                    if (wvMainWeb.getScrollY() == 0){
                        //Toast.makeText(MainActivity.this, "TOP", Toast.LENGTH_SHORT).show();
                        swipeLayout.setEnabled(true);
                    } else {
                        swipeLayout.setEnabled(false);
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        wvMainWeb.goBack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "menu " + item.getItemId(), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }
}
