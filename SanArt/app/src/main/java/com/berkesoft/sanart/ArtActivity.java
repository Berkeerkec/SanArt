package com.berkesoft.sanart;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.berkesoft.sanart.databinding.ActivityArtBinding;
import com.berkesoft.sanart.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {
    private ActivityArtBinding binding;
    //3.2) Burada kullanacağım launcherlarımı tanımlıyorum.
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null);
        setContentView(view);
        registerLauncher();

        //9.3) intent değerlerimi alıyorum ve if komutuyla new mi yoksa old mu geldi diye ayırma işlemi yapıyorum.
        Intent intent = getIntent();
        String info= intent.getStringExtra("info");

        if (info.equals("new")){
            //Yeni kayıt yapacak.
            //Burada pek bişey yapmıyoruz sadece kutucukların içlerini boşaltıyoruz.
            binding.artText.setText("");
            binding.artistText.setText("");
            binding.yearText.setText("");
            binding.button.setVisibility(View.VISIBLE);

            binding.imageView.setImageResource(R.drawable.selectimage1);

        } else{
            //değilse id gönderip veri çekecek.
            int artId = intent.getIntExtra("artId",0);
            binding.button.setVisibility(View.INVISIBLE);
            //Burada bana id gönderdi bende bu id'ye göre databaseden veri çekeceğim.

            try {
                //Gelecek olan id'nin ne olduğunu bilmediğim için soru işareti bıraktım. Virgül koyduktan sonra String dizisi içerisine artId'imi yazıyorum.
                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", new String[] {String.valueOf(artId)});

                int artNameIx = cursor.getColumnIndex("artname");
                int artistNameIx = cursor.getColumnIndex("artistname");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while(cursor.moveToNext()){
                    binding.artText.setText(cursor.getString(artNameIx));
                    binding.artistText.setText(cursor.getString(artistNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    binding.imageView.setImageBitmap(bitmap);

                }
                cursor.close();


            }catch(Exception e){
                e.printStackTrace();
            }

        }



    }


    public void save(View view){
        //5) Uygulama içinde kutucukların içine yazılan verileri bir string objesine eşitliyorum.
        String artName = binding.artText.getText().toString();
        String artistName = binding.artistText.getText().toString();
        String year = binding.yearText.getText().toString();

        //5.2) Oluşturduğum makeSmallerImage metodumu bir bitmap objesine eşitleyip, içine görselim olan selectedImage'imi giriyorum ve size yani boyutunu giriyorum.
        Bitmap smallImage = makeSmallerImage(selectedImage,300);

        //5.3)SQlite'a bu görseli koyabilmem için veriyi 0 ve 1 lere çevirmem gerekir. bunun için ByteArrayOutputStream'i kullanacağım
        // ByteArrayOutputStream'i inişılayz ediyoruz.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.JPEG,50,outputStream);

        //toByteArray() = veriyi byte dizisine çevirdim daha sonra byte objesine eşitledim.
        byte[] byteArray = outputStream.toByteArray();

        try {

            //6) verilerimizi sqlite'a kaydediyoruz. Yukarıda sınıfta Sqlite'ı tanımladım.
           //EK NOT: Normalde databasemi burada kurmuştum fakat diğer  metotlarda da kullanacağım için onCreate altına aldım.
            database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)");

            //6.1) Biz verileri sonradan kayıt edeceğimiz için şuan birşey kaydetmeyeceğiz.
            //6.2) Aşağıdaki gibi insert into yazdıktan sonra SqliteStatement kullanıyoruz.
            //6.3)  SQLiteStatement = SQLiteta bize binding işlevi görüyor.
            // ? işaretleri yerine save metodu içinde başta tanımladığımız artname, artisname, year ve image'i bağlıyoruz.
            String sqlString = "INSERT INTO arts (artname, artistname, year, image) VALUES (?,?,?,?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,artName);
            sqLiteStatement.bindString(2,artistName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();

        } catch (Exception e){
            e.printStackTrace();
        }

        //6.4) Kayıt yaptıktan sonra bu aktiviteyi kapatıp en baştaki aktiviteye döneceğiz. Intenti kullanıyorum.
        Intent intent = new Intent(ArtActivity.this, MainActivity.class);
        //6.5) addflags = bayrak ekleme. FLAG_ACTIVITY_CLEAR_TOP = Gideceğim aktivite hariç tüm aktiviteleri kapat demek.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }



    //5.1) Görsel boyutlarım büyük olursa uygulama hata verebilir. Bu yüzden görsellerimizi küçültme işlemi yapıyoruz.
    //5.1.1) Bunun için bir bitmap metotu oluşturuyorum.
    public Bitmap makeSmallerImage(Bitmap image, int maximumSize){
        //5.1.2) Görselimizin genişlik ve yüksekliğini int bir obje oluşturarak eşitliyorum.
        int width = image.getWidth();
        int height = image.getHeight();

        //5.1.3) Daha sonra if döngüsüyle görseli küçültme işlemi yapıyorum.
        float bitmapRatio =(float) width/ (float) height;
        if (bitmapRatio > 1){
            //Yatay bir foto
            width = maximumSize;
            height = (int) (width/bitmapRatio);

        } else{
            // Dikey bir foto
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        //5.1.4) ScaledBitmap= daha büyük ya da daha küçük bir bitmap ayarla demek.
        //5.1.5) Oluşturduğum width ve height'ımı giriyorum ve döndür diyorum.
        return image.createScaledBitmap(image, width, height, true);

    }





    //2) İzin istemeden önce soldaki Project kısmında app klasörü içindeki Manifest klasörüne gidip -
    // AndroidManifest xml'ine giriyoruz ve içerisine depolama iznimizi yazıyoruz.

    public void selectImage(View view){

        // 2.1) İzinimi if komutu içerisinine yazıyorum ve ContextCompat diyorum. Bunu dememin sebebi 19 Apı -
        // aşağısındaki telefonlarda izin almaya gerek olmadığı için ContexCompat bunu bizim için kontrol edecek.
        // !!! checkSelfPermission = İzinleri kontrol et demek.
        // 2.2) checkSelfPermission bizden Context ve kontrol edeceğimiz iziniz soruyor. Bizde giriyoruz.
        // 2.3) PackageManager.PERMISSION_GRANTED (Paket yöneticim izin verdi)'yi checkSelfPermission'a eşit değildir diyerek şunu diyorum. Eğer depolama izni verilmemişse diyorum.

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //2.4) Eğer izin verilmemişse izin iste.
            //request permission (izin iste)

            // 2.5) Android, eğer kullanıcı izin vermezse izinin mantığını anlatmamızı istemektedir.
            // shouldShowRequestPermissionRationale = izin isteme mantığını göstereyim mi? demek.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                // 2.5.1) Tıkladığımızda bize işin mantığını söyleyecek bir alertDialog yada snackbar yazmamızı istiyor.
                //2.6) Snackbarda ilk önce bir görünüm istiyor. Bizim metotumuzda view  görünümü var.
                //2.6.1) Daha sonra kullanıcıya söylemem gereken uyarıyı yazıyorum.
                //2.6.2) son olarak Snackbar.LENGTH_INDEFINITE diyerek kullanıcı okeye basana kadar ekranda kalsın diyorum.
                //2.6.3) Snackbarın sonuna show diyip göstereblirim am ben buton koyacağım ve bu butona basıldığında tekrar izin istemesini sağlayacağım.
                //2.6.4) setAction diyorum. Texti yazıyorum ve onClickListener diyerek tıklandığında ne olacağını yazıyorum.

                Snackbar.make(view, "Galeriye gitmek için izin gerekli!", Snackbar.LENGTH_INDEFINITE ).setAction("İzin ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //2.6.5) tekrar izin isteme komutumu buraya yazacağım.
                        //request permission.
                        //4.3) Burada izin isteme launcher'ımı kullandım
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();

            } else {
                // 2.5.2) Ya da direkt izin iste diyeceğiz.
                // request permission
                //4.4) Burada izin isteme launcher'ımı kullandım.
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

        } else{
           //eğer izin verilmişse galeriye git.
           // gallery

            //3) Galeriye gitmek için Intent kullanıyorum. Intent içine 1 aksiyon ve 1 tane Urı yazıyorum.
            //Action=İntentin yapması gereken şey. Biz burda  Pick'i seçiyoruz. Pick=almak isteğimiz şeyi tut anlamına geliyor.
            //Urı= gitmek istediğim adres. Görseli almak istediğim adresi yazıyorum. Buna Uri deniyor.
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //4.1) Burada da galeriye gideceğim için activityResultLauncher'ı kullandım
            activityResultLauncher.launch(intentToGallery);
            //3.1)Şimdi biz galeriye gittik ama ne yapacağımızı yazmadık. bunun için ACtivityResultLauncher kullanacağız.
            //3.1.1) ActivityResultLauncher = Bir aktiviteye, galleriye ya da izin isterken vs. ne yapacağımızı yazdığımız launcherdır.
            //3.1.2) İlk önce sınıfın içinde tanımlamam lazım.

        }


    }



    //3.2.1) Yukarıda sınıf içinde tanıımladığım iki launcherı onCreate altında tanımlamam gerekir ama onCreate karışmasın diye burda private bir sınıf içerisinde yazıp onCreate içinde tanımlayacağım.

    private void registerLauncher(){
        //3.2.2) registerForActivityResult = İşin sonunda cevap alacağımız bir işlem yapacağımızı söylüyoruz burada.
        //Benden 1 contract istiyor. Yani bu launcherla ne yapacağımızı soruyor. Biz de izin isteyeceğimizi yazıyoruz içine.
        //VE son olarak callback istiyor. Ne olacağını yazıyoruz.
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
             if(result){
                 //İzin verilmişse ne olacak?
                 //PErmission Granted
                 Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                 //4) Launcherlarımı oluşturduğuma göre burada kulanabilirim.
                 activityResultLauncher.launch(intentToGallery);
             } else{
                 //İzin verilmemişse ne yapılsın?
                 //Permission Denied.
                 Toast.makeText(ArtActivity.this, "İzin gerekli!", Toast.LENGTH_LONG).show();
             }

            }
        });

        //3.3) Burada ise launcherı başlatacağım.
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                //3.3.1) Kullanıcı eğer görseli seçtiyse (if)
                if (result.getResultCode() == RESULT_OK){
                    //3.3.2) İçeriği intent olan sonucumu(result) alıyorum ve bir intent objesine (intentFromData) eşitliyorum.
                    Intent intentFromData = result.getData();
                    //3.3.3) Eğer benim intentFromDatam boş birşey değilse ne yapacağımı yazıyorum.
                    if (intentFromData != null){
                        //3.3.4) Eğer boş değilse tüm datayı al diyorum fakat benim getDatamın içeriği Uri olduğu için Uri den bir obje oluşturup eşitliyorum.
                        //Bununla birlikte kullanıcının seçtiği görselin nerede  olduğunu biliyoruz.
                        Uri imageData = intentFromData.getData();

                        //! binding.imageView.setImageURI(imageData); diyip imageView içinde gösterebiliridik.
                        //! fakat imageData bir Uri ve bizim aslında imageViewda göstermek isteğimiz görselin verisi.
                        //! Bunun için bitmap oluşturup bunu veri depolamasına kaydetmemiz lazım.

                        //3.4) try and catch içinde bitmapimizi oluşturuyoruz.
                        try {
                            //ImageDecoder= veriyi bul görsele çevir sınıfı.
                            //3.4.1) createSource içerisine aynı layout inflater gibi getContentResolver yazıyoruz ve imageDatamızı yazıyoruz.
                            ImageDecoder.Source source = ImageDecoder.createSource(ArtActivity.this.getContentResolver(), imageData);
                            //3.4.2) daha sonra ImageDecod'i kullanarak bitmapa çeviriyorum. ve bu bitmpi heryerde kullanabilmem için yukarda sınıf içerisine bitmap oluşturuyorum.
                            selectedImage = ImageDecoder.decodeBitmap(source);
                            binding.imageView.setImageBitmap(selectedImage);


                        }catch(Exception e){
                            e.printStackTrace();

                        }


                    }

                }
            }
        });


    }


}