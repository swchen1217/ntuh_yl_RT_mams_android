package com.swchen1217.ntuh_yl_rt_mdms;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.data.CellInfo;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.column.ColumnInfo;
import com.bin.david.form.data.format.bg.BaseBackgroundFormat;
import com.bin.david.form.data.format.bg.BaseCellBackgroundFormat;
import com.bin.david.form.data.format.bg.IBackgroundFormat;
import com.bin.david.form.data.style.FontStyle;
import com.bin.david.form.data.table.TableData;
import com.bin.david.form.listener.OnColumnClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InquireStatusActivity extends AppCompatActivity {

    SmartTable table;
    SwipeRefreshLayout mSwipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inquire_status);
        table=findViewById(R.id.table);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setColorSchemeColors(Color.RED);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeLayout.setRefreshing(false);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(new SyncDB(InquireStatusActivity.this).SyncDeviceTable(false)){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(InquireStatusActivity.this, "已重新整理", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new SyncDB(InquireStatusActivity.this).SyncDeviceTable(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        show();
    }

    void show(){
        Column<String> DID = new Column<>("設備ID", "DID");
        Column<String> category = new Column<>("分類", "category");
        Column<String> model = new Column<>("型號", "model");
        Column<String> number = new Column<>("編號", "number");
        Column<String> user = new Column<>("使用者", "user");
        Column<String> position = new Column<>("位置", "position");
        Column<String> status = new Column<>("狀態", "status");
        Column<String> LastModified = new Column<>("修改日期", "LastModified");
        List<DeviceTable> data =new ArrayList<>();

        SQLite sql =new SQLite(this);
        Cursor c = sql.select("device_tb",null, null, null, null, null);
        int rows_num = c.getCount();
        if (rows_num != 0) {
            c.moveToFirst();
            for (int j = 0; j < rows_num; j++) {
                String str = "";
                data.add(new DeviceTable(c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),DeviceStatus.StatusStr[Integer.parseInt(c.getString(6))],c.getString(7)));
                c.moveToNext();
            }
        }

        TableData<DeviceTable> td=new TableData<>("Test",data,DID,category,model,number,user,position,status,LastModified);
        table.setTableData(td);
        table.getConfig().setContentStyle(new FontStyle(50, Color.BLACK));
        table.getConfig().setContentCellBackgroundFormat(new BaseCellBackgroundFormat<CellInfo>() {
            @Override
            public int getBackGroundColor(CellInfo cellInfo) {
                /*Log.d("cellInfo_value",cellInfo.value);
                Log.d("cellInfo_col",cellInfo.col+"");
                Log.d("cellInfo_column",cellInfo.column.toString());
                Log.d("cellInfo_data",cellInfo.data.toString());
                Log.d("cellInfo_row",cellInfo.row+"");*/
                if(cellInfo.row%2==1)
                    return ContextCompat.getColor(InquireStatusActivity.this,R.color.bg);
                else
                    return 0;
            }
        });
        table.setZoom(true);
        table.setOnColumnClickListener(new OnColumnClickListener() {
            @Override
            public void onClick(ColumnInfo columnInfo) {
                Log.d("onClick",columnInfo.value);
            }
        });
    }
}
