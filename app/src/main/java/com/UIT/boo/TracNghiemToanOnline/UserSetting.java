package com.UIT.boo.TracNghiemToanOnline;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.Lego.TracNghiemToanOnline.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class UserSetting extends android.support.v4.app.DialogFragment {
    public static final String TAG = "UserSetting";

    public UserSetting() {
        // Required empty public constructor
    }
    private final int PICK_IMAGE_REQUEST = 71;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference databaseRefence = FirebaseDatabase.getInstance().getReference();
    private Uri filepath;
    FirebaseUser user = firebaseAuth.getCurrentUser();
    EditText edt_fullname, edt_phone;
    private Button btn_xoay;
    CircleImageView cimv_avatar;
    Button btn_save;
    String email, fullname, imagebase64, phone,username;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);
        edt_fullname = view.findViewById(R.id.editTextName);
        edt_phone = view.findViewById(R.id.editTextPhone);
        btn_save = view.findViewById(R.id.btn_save);
        btn_xoay = view.findViewById(R.id.btn_xoay2);
        cimv_avatar = view.findViewById(R.id.imgAvatar);
        imagebase64 = getString(R.string.ic_user);

        cimv_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullname = edt_fullname.getText().toString().trim();
                phone = edt_phone.getText().toString().trim();

                if(TextUtils.isEmpty(fullname)) // k nhập tài khoản sẽ báo lỗi
                {
                    Toast.makeText(getActivity(), "Please enter your fullname", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(phone)) // k nhập pass sẽ báo lỗi
                {
                    Toast.makeText(getActivity(), "Please enter your phone number", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(imagebase64)) // k nhập pass sẽ báo lỗi
                {
                    Toast.makeText(getActivity(), "Please choose your picture", Toast.LENGTH_LONG).show();
                    return;
                }

                final Handler handler = new Handler();
                final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                pDialog.setTitleText("Đang lưu...");
                pDialog.setCancelable(false);
                pDialog.show();
                UserInformation userInformation = new UserInformation(MainActivity.email, fullname, imagebase64, phone, MainActivity.username);
                databaseRefence.child("Users").child(user.getUid()).child("Information").setValue(userInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            pDialog.setTitleText("Lưu thành công!");
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    pDialog.dismissWithAnimation();
                                }
                            }, 500);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getDialog().dismiss();
                                }
                            }, 1500);
                        }
                        else
                        {
                            pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            pDialog.setTitleText("Lưu thất bại!");
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    pDialog.dismissWithAnimation();
                                }
                            }, 500);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getDialog().dismiss();
                                }
                            }, 1500);
                        }
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data !=null && data.getData() != null)
        {
            filepath = data.getData();
            try
            {
                final Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filepath);
                cimv_avatar.setImageBitmap(bitmap);
//                if(bitmap != null)
//                {
//                    btn_xoay.setVisibility(View.VISIBLE);
//                    btn_xoay.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            cimv_avatar.setRotation(cimv_avatar.getRotation()+90F);
//                        }
//                    });
//                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] b = baos.toByteArray();
                imagebase64 = Base64.encodeToString(b, Base64.DEFAULT);
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
