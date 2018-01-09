package com.example.user.youandi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class Youandi extends Fragment {
    FloatingActionButton loginBtn;
    int room_num;
    public Youandi() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_youandi, container, false);

        loginBtn=(FloatingActionButton) view.findViewById(R.id.youandi_login);
        loginBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                login_show();
            }
        });
        return view;
    }
    void login_show() {
        final EditText edittext = new EditText(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Login");
        builder.setMessage("방번호를 입력하세요.");
        builder.setView(edittext);
        builder.setPositiveButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(),"방번호 : "+edittext.getText().toString() ,Toast.LENGTH_LONG).show();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(),"취소되었습니다." ,Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
    }

}