package com.yigeon.subscriptioncheck;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreateSubActivity extends Activity {
    static final int RESULT_OK = 2;
    Button btnCancel, btnOk;
    EditText editPrice;
    AutoCompleteTextView editName;
    RadioButton rBtnMonth, rBtnYear;
    DatePicker dpStartDate;
    Calendar calendar = Calendar.getInstance();
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.createservice);

        btnCancel = (Button) findViewById(R.id.btnCreateCancel);
        btnOk = (Button) findViewById(R.id.btnCreateOK);
        editName = (AutoCompleteTextView) findViewById(R.id.editName);
        editPrice = (EditText) findViewById(R.id.editPrice);
        rBtnMonth = (RadioButton) findViewById(R.id.rBtnMonth);
        rBtnYear = (RadioButton) findViewById(R.id.rBtnYear);
        dpStartDate = (DatePicker) findViewById(R.id.dpStartDate);

        String[] subsItem = {"멜론", "넷플릭스", "왓챠플레이", "유튜브 프리미엄", "벅스", "지니", "VIVE", "애플뮤직", "DropBox", "구글 드라이브"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, subsItem);
        editName.setAdapter(adapter);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                int day = dpStartDate.getDayOfMonth();
                int month = dpStartDate.getMonth() + 1;
                int year = dpStartDate.getYear();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH) + 1;
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                Boolean check = null;


                intent.putExtra("service_name", editName.getText().toString());
                intent.putExtra("service_price", editPrice.getText().toString());
                intent.putExtra("service_start_date", year + "-" + month + "-" + day);

                try {
                    Date inputDate = dateFormat.parse(year + "-" + month + "-" + day);
                    Date currentDate = dateFormat.parse(currentYear + "-" + currentMonth + "-" + currentDay);

                    check = checkDate(inputDate, currentDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                if (rBtnMonth.isChecked()) {
                    intent.putExtra("payment_type", "월 결제");
                    if (check) {                                                                                // 데이트피커를 통해 선택한 값이 현재 날짜보다 큰 경우
                        if ((month + 1) > 12) {
                            intent.putExtra("service_next_date", (year + 1) + "-" + 1 + "-" + day); //12월이 넘어가면 다음년도로 변경
                        } else {
                            intent.putExtra("service_next_date", year + "-" + (month + 1) + "-" + day);
                        }
                    } else {                                                                                        // 데이트피커를 통해 선택한 값이 현재 날짜보다 작은 경우
                        if ((month + 1) > 12) {
                            if (day < currentDay) {
                                intent.putExtra("service_next_date", currentYear + "-" + (currentMonth + 1) + "-" + day); // 일이 현재 날짜 보다 작을 경우 다음달로 넘김
                            }
                        } else if (day > currentDay && month < currentMonth) {
                            intent.putExtra("service_next_date", currentYear + "-" + currentMonth + "-" + day);

                        } else {
                            intent.putExtra("service_next_date", currentYear + "-" + (currentMonth + 1) + "-" + day);
                        }

                    }


                }


                if (rBtnYear.isChecked()) {
                    intent.putExtra("payment_type", "년 결제");
                    if (check) {
                        intent.putExtra("service_next_date", (year + 1) + "-" + month + "-" + day);
                    } else if (day > currentDay && month < currentMonth) {
                        intent.putExtra("service_next_date", currentYear + "-" + currentMonth + "-" + day);
                    } else {
                        intent.putExtra("service_next_date", (currentYear + 1) + "-" + currentMonth + "-" + day);
                    }

                }

                setResult(RESULT_OK, intent);
                finish();


            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });
    }

    // true 이면 inputDate에 날짜 더하기
    // false면 currentDate에 더하기
    static boolean checkDate(Date inputDate, Date currentDate) {
        int compare = inputDate.compareTo(currentDate);
        if (compare > 0) {
            return true;
        }
        if (compare < 0) {
            return false;
        }
        return true;

    }

}
