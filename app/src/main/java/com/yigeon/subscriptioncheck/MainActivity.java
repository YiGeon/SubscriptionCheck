package com.yigeon.subscriptioncheck;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.yigeon.subscriptioncheck.CreateSubActivity.checkDate;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    static final int REQ_ADD_SERVICE = 2;
    static final int REQ_MODIFY_SERVICE = 3;
    static int numinfo = 0;
    ViewGroup moView;
    ScrollView activitymain;
    LinearLayout rootLayout, subInfo;
    TextView tvGuide;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("구독 결제 확인 앱");


        rootLayout = (LinearLayout) findViewById(R.id.root);
        subInfo = (LinearLayout) findViewById(R.id.subinfo);
        activitymain = (ScrollView) findViewById(R.id.activity_main);

        readFile();
        showGuide();

        Log.d(TAG, "넘인포 = " + numinfo);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {         // 상단 메뉴 세팅
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu1, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {              // 메뉴 클릭시 이벤트
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.itemCreate:
                Intent intent = new Intent(getApplicationContext(), CreateSubActivity.class);
                startActivityForResult(intent, REQ_ADD_SERVICE);
                return true;
            default:
                return false;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {       // 다른 엑티비티에서 정보 받기 & 다른 엑티비티에서 버튼 입력시 메소드 실행
        super.onActivityResult(requestCode, resultCode, intent);
        String serName, serPrice, serStartDate, serNextDate, payType;
        switch (resultCode) {
            case 2:
                serName = intent.getStringExtra("service_name");
                serPrice = intent.getStringExtra("service_price");
                serStartDate = intent.getStringExtra("service_start_date");
                serNextDate = intent.getStringExtra("service_next_date");
                payType = intent.getStringExtra("payment_type");
                addView(serName, serPrice, serStartDate, serNextDate, payType);
                saveFile();                                                                 // 뷰 생성 후 파일 새로 저장
                showGuide();

                break;
            case 3:
                serName = intent.getStringExtra("service_name");
                serPrice = intent.getStringExtra("service_price");
                serStartDate = intent.getStringExtra("service_start_date");
                serNextDate = intent.getStringExtra("service_next_date");
                payType = intent.getStringExtra("payment_type");
                modifyView(serName, serPrice, serStartDate, serNextDate, payType);
                saveFile();                                                             // 수정 후 파일 새로 저장
            default:
                break;
        }
    }

    private void addView(String serName, String serPrice, String serStartDate, String serNextDate, String payType) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup subInfoView = (ViewGroup) inflater.inflate(R.layout.subinfo, null);

        TextView tvSerName = (TextView) subInfoView.findViewById(R.id.tvSerName);
        TextView tvSerPrice = (TextView) subInfoView.findViewById(R.id.tvSerPrice);
        TextView tvStarDate = (TextView) subInfoView.findViewById(R.id.tvStarDate);
        TextView tvNextDate = (TextView) subInfoView.findViewById(R.id.tvNextDate);
        TextView tvPayType = (TextView) subInfoView.findViewById(R.id.tvPayType);
        tvSerName.setText(serName);
        tvSerPrice.setText(serPrice);
        tvStarDate.setText(serStartDate);
        tvNextDate.setText(serNextDate);
        tvPayType.setText(payType);
        setColorinThreeDays(serNextDate, subInfoView);

        subInfoView.setTag(serName);
        subInfoView.setId(numinfo);
        rootLayout.addView(subInfoView);
        subInfoView.setOnClickListener(this);

        numinfo++;

    }

    @Override
    public void onClick(View view) {
        showDialog(view);
    }

    private void showDialog(final View view) {
        final String[] diaItems = new String[]{"수정", "삭제"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(view.getTag() + "관리메뉴");
        builder.setItems(diaItems, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (diaItems[which].equals("삭제")) {
                    removeView(view);
                    saveFile();                                       // 뷰 삭제 후 파일 새로 저장
                    showGuide();
                } else if (diaItems[which].equals("수정")) {
                    showModifyView(view);


                }

            }
        });
        builder.show();
    }

    @SuppressLint("ResourceType")
    private void removeView(View view) {
        ViewGroup subInfoView = (ViewGroup) findViewById(view.getId());
        rootLayout.removeView(subInfoView);
        numinfo--;


        for (int i = view.getId(); i < numinfo + 1; i++) {            //  뷰 삭제시, 해당 뷰보다 id값이 큰 뷰의 id를 -1 해줌
            ViewGroup numAddView = (ViewGroup) findViewById(i + 1);
            if (numAddView == null) {                                           // 반복 돌다 해당 뷰가 없으면 반복 종료
                return;
            }
            numAddView.setId(i);

        }

    }

    private void showModifyView(View view) {

        ViewGroup subInfoView = (ViewGroup) findViewById(view.getId());
        TextView tvSerName = (TextView) subInfoView.findViewById(R.id.tvSerName);
        TextView tvSerPrice = (TextView) subInfoView.findViewById(R.id.tvSerPrice);
        TextView tvStarDate = (TextView) subInfoView.findViewById(R.id.tvStarDate);
        TextView tvNextDate = (TextView) subInfoView.findViewById(R.id.tvNextDate);
        TextView tvPayType = (TextView) subInfoView.findViewById(R.id.tvPayType);

        moView = subInfoView;

        Intent intent = new Intent(getApplicationContext(), ModifySubActivity.class);
        intent.putExtra("service_name", tvSerName.getText().toString());
        intent.putExtra("service_price", tvSerPrice.getText().toString());
        intent.putExtra("service_start_date", tvStarDate.getText().toString());
        intent.putExtra("service_next_date", tvNextDate.getText().toString());
        intent.putExtra("payment_type", tvPayType.getText().toString());

        startActivityForResult(intent, REQ_MODIFY_SERVICE);

    }

    private void modifyView(String serName, String serPrice, String serStartDate, String serNextDate, String payType) {
        TextView tvSerName = (TextView) moView.findViewById(R.id.tvSerName);
        TextView tvSerPrice = (TextView) moView.findViewById(R.id.tvSerPrice);
        TextView tvStarDate = (TextView) moView.findViewById(R.id.tvStarDate);
        TextView tvNextDate = (TextView) moView.findViewById(R.id.tvNextDate);
        TextView tvPayType = (TextView) moView.findViewById(R.id.tvPayType);
        tvSerName.setText(serName);
        tvSerPrice.setText(serPrice);
        tvStarDate.setText(serStartDate);
        tvNextDate.setText(serNextDate);
        tvPayType.setText(payType);
        setColorinThreeDays(serNextDate, moView);
    }

    private void saveFile() {       // 뷰 갯수 만큼하면서 현재 상태를 새로 저장
        try {

            File file = new File(getFilesDir(), "subs.txt");
            FileWriter fw = new FileWriter(file, false);
            BufferedWriter bufwr = new BufferedWriter(fw);

            String name = null, price = null, start = null, next = null, type = null;
            for (int i = 0; i < numinfo + 1; i++) {
                ViewGroup subInfoView = (ViewGroup) findViewById(i);
                if (subInfoView == null) {                                                         // 반복 돌다 해당 뷰가 없으면 반복 종료
                    return;
                }
                TextView tvSerName = (TextView) subInfoView.findViewById(R.id.tvSerName);
                TextView tvSerPrice = (TextView) subInfoView.findViewById(R.id.tvSerPrice);
                TextView tvStarDate = (TextView) subInfoView.findViewById(R.id.tvStarDate);
                TextView tvNextDate = (TextView) subInfoView.findViewById(R.id.tvNextDate);
                TextView tvPayType = (TextView) subInfoView.findViewById(R.id.tvPayType);

                name = tvSerName.getText().toString();
                price = tvSerPrice.getText().toString();
                start = tvStarDate.getText().toString();
                next = tvNextDate.getText().toString();
                type = tvPayType.getText().toString();

                bufwr.write(name);
                bufwr.newLine();

                bufwr.write(price);
                bufwr.newLine();

                bufwr.write(start);
                bufwr.newLine();

                bufwr.write(type);
                bufwr.newLine();

                bufwr.flush();
                Log.d(TAG, "넘인포 = " + numinfo);
            }
            bufwr.close();
            fw.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 파일 4줄씩 읽어서 뷰 생성 할 수 있는 메소드 만들기!!

    private void readFile() {

        try {
            String subName, subPrice, subStart, serType;
            File file = new File(getFilesDir(), "subs.txt");
            FileReader fr = new FileReader(file);
            BufferedReader bufrd = new BufferedReader(fr);


            if (file.exists()) {
                while ((subName = bufrd.readLine()) != null) {
                    subPrice = bufrd.readLine();
                    subStart = bufrd.readLine();
                    serType = bufrd.readLine();

                    System.out.println(subName);
                    System.out.println(subPrice);
                    System.out.println(subStart);
                    System.out.println(serType);

                    addViewFromFile(subName, subPrice, subStart, serType);
                }

                bufrd.close();
                fr.close();
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addViewFromFile(String serName, String serPrice, String serStartDate, String payType) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup subInfoView = (ViewGroup) inflater.inflate(R.layout.subinfo, null);

        TextView tvSerName = (TextView) subInfoView.findViewById(R.id.tvSerName);
        TextView tvSerPrice = (TextView) subInfoView.findViewById(R.id.tvSerPrice);
        TextView tvStarDate = (TextView) subInfoView.findViewById(R.id.tvStarDate);
        TextView tvNextDate = (TextView) subInfoView.findViewById(R.id.tvNextDate);
        TextView tvPayType = (TextView) subInfoView.findViewById(R.id.tvPayType);
        tvSerName.setText(serName);
        tvSerPrice.setText(serPrice);
        tvStarDate.setText(serStartDate);

        tvPayType.setText(payType);


        tvNextDate.setText(calDate(serStartDate, payType));     // 앱 실행시 실행한 날짜 기준으로 다음 결재일 다시 설정
        setColorinThreeDays(calDate(serStartDate, payType), subInfoView);


        subInfoView.setTag(serName);
        subInfoView.setId(numinfo);
        rootLayout.addView(subInfoView);
        subInfoView.setOnClickListener(this);
        numinfo++;

    }


    private String calDate(String startDate, String payType) {
        try {
            Calendar calendar = Calendar.getInstance();
            Calendar start = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(startDate);
            start.setTime(date);

            int day = start.get(Calendar.DAY_OF_MONTH);
            int month = start.get(Calendar.MONTH) + 1;
            int year = start.get(Calendar.YEAR);

            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

            Date inputDate = dateFormat.parse(year + "-" + month + "-" + day);
            Date currentDate = dateFormat.parse(currentYear + "-" + currentMonth + "-" + currentDay);


            boolean check = checkDate(inputDate, currentDate);

            String nextDate = null;


            if (payType.equals("월 결제")) {
                if (check) {                                                                                // 데이트피커를 통해 선택한 값이 현재 날짜보다 큰 경우
                    if ((month + 1) > 12) {
                        nextDate = (year + 1) + "-" + 1 + "-" + day; //12월이 넘어가면 다음년도로 변경
                    } else {
                        nextDate = year + "-" + (month + 1) + "-" + day;
                    }
                } else {                                                                                        // 데이트피커를 통해 선택한 값이 현재 날짜보다 작은 경우
                    if ((month + 1) > 12) {
                        if (day < currentDay) {
                            nextDate = currentYear + "-" + (currentMonth + 1) + "-" + day; // 일이 현재 날짜 보다 작을 경우 다음달로 넘김
                        }
                    } else if (day > currentDay && month < currentMonth) {
                        nextDate = currentYear + "-" + currentMonth + "-" + day;
                    } else {
                        nextDate = currentYear + "-" + (currentMonth + 1) + "-" + day;
                    }

                }


            }

            if (payType.equals("년 결제")) {
                if (check) {
                    nextDate = (year + 1) + "-" + month + "-" + day;
                } else if (day > currentDay && month < currentMonth) {
                    nextDate = currentYear + "-" + currentMonth + "-" + day;
                }else {
                    nextDate = (currentYear + 1) + "-" + currentMonth + "-" + day;
                }

            }

            return nextDate;


        } catch (ParseException e) {
            e.printStackTrace();
        }


        return null;
    }

    private void showGuide() {
        tvGuide = (TextView) findViewById(R.id.tvGuide);
        if (numinfo != 0) {
            tvGuide.setVisibility(View.GONE);
        } else {
            tvGuide.setVisibility(View.VISIBLE);
        }

    }

    private void setColorinThreeDays(String nextDay, View view) {
        try {
            Calendar current = Calendar.getInstance();
            Calendar next = Calendar.getInstance();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String currentDay = current.get(Calendar.YEAR) + "-" + (current.get(Calendar.MONTH) + 1) + "-" + current.get(Calendar.DAY_OF_MONTH);

            Date nextDate = dateFormat.parse(nextDay);
            Date currentDate = dateFormat.parse(currentDay);



            next.setTime(nextDate);
            current.setTime(currentDate);



            long gapSec = (next.getTimeInMillis() - current.getTimeInMillis()) /1000;
            long gapDay = gapSec / (24 * 60 * 60);

            if (gapDay <= 3) {
                view.setBackgroundColor(Color.rgb(237, 177, 81));
            } else {
                view.setBackgroundColor(Color.WHITE);
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}