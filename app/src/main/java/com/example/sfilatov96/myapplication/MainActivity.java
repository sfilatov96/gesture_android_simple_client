package com.example.sfilatov96.myapplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.content.pm.LauncherApps;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity {
    private String URL;
    Button send_button;
    EditText address;
    Bitmap selectedImage;
    TextView textView;
    String base64String;
    File filephoto;
    //Объявляем используемые переменные:
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private ImageView imageView;
    private final int Pick_image = 1;
    OkHttpClient client = new OkHttpClient();


    Call post(String url, File file, Callback callback) {
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("image/png"), file))
                .addFormDataPart("other_field", "other_field_value")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    private byte[] bitmapToBytes(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int size = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte [] byteArray = byteBuffer.array();
        return byteArray;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //Связываемся с нашим ImageView:
        imageView = (ImageView)findViewById(R.id.imageView);
        textView =  (TextView)findViewById(R.id.textView);

        //Связываемся с нашей кнопкой Button:
        Button PickImage = (Button) findViewById(R.id.button);
        //Настраиваем для нее обработчик нажатий OnClickListener:
        PickImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                //Вызываем стандартную галерею для выбора изображения с помощью Intent.ACTION_PICK:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                //Тип получаемых объектов - image:
                photoPickerIntent.setType("image/*");
                //Запускаем переход с ожиданием обратного результата в виде информации об изображении:
                startActivityForResult(photoPickerIntent, Pick_image);
            }
        });
        send_button = (Button)findViewById(R.id.button_send);
        address = (EditText)findViewById(R.id.editText);
        address.setText("http://192.168.1.35:5000/api/photo");
        send_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String address_text = address.getText().toString();

                post(address_text, filephoto, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            final String responseStr = response.body().string();
                            Log.d("success",responseStr);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub

                                    textView.setText(responseStr);
                                }});
                        }
                    }

                });
                    //textView.setText(answer);




            }
        });

    }

    private void set_text(String str){
        textView.setText(str);
    }

    //Обрабатываем результат выбора в галерее:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case Pick_image:
                if(resultCode == RESULT_OK){
                    try {
                        //Получаем URI изображения, преобразуем его в Bitmap
                        //объект и отображаем в элементе ImageView нашего интерфейса:
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream;

                        imageStream = getContentResolver().openInputStream(imageUri);

                        selectedImage = BitmapFactory.decodeStream(imageStream);
                        filephoto = new File(imageUri.getPath());
                        imageView.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }}}