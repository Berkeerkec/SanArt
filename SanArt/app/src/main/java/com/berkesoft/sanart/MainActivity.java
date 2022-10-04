package com.berkesoft.sanart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.berkesoft.sanart.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    ArrayList<Art> artArrayList;
    ArtAdapter artAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        artArrayList = new ArrayList<>();

        //8.5) RecyclerView a layoutManager'la Linear olacağını belirttim.
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //8.5.1) Burada ilk önce yukarıdaki sınıfımın içine ArtAdaptör sınıfımı tanımladım. Daha sonra aşağıda artAdapteri inişılayz ettim ve artAdapteri recyclerView'a bağladım.
        //8.5.2) yeni veriler geldiğinde recyclerView kendini güncellesin diye getData() metodunun sonuna notifyDataSetChanged(); yazmamız gerekir.
        artAdapter = new ArtAdapter(artArrayList);
        binding.recyclerView.setAdapter(artAdapter);
        getData();
    }

    //7) ArtActivityden verilerimizi çekip Main Activityde göstereceğiz.
    private void getData(){

        try {
            //7.1)MainActivity de sadece isim ve id verilerilerini göstereceğiz.
            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null);
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM arts",null);
            int nameIx = cursor.getColumnIndex("artname");
            int idIx = cursor.getColumnIndex("id");

            while (cursor.moveToNext()){

                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);

                //7.2) Bu verileri bir sınıf oluşturup constructor edip burada bir arrayliste kaydedeceğiz ve recyclerView da göstereceğiz.
                Art art = new Art(name, id);
                artArrayList.add(art);
            }
            //8.6) Yeni veriler geldiğinde recyclerView'ın kendini güncellemesi için notifyDataSetChanged() yazmamız gerekir.
            artAdapter.notifyDataSetChanged();
            //7.3) En son cursor'ımızı kapatıyoruz.
            cursor.close();


        } catch (Exception e){
            e.printStackTrace();
        }



    }




    //1) ilk ana sayfada yeni bir art ekleyebilmek için xml oluşturuyorum. bu xml'e item ve title ekliyorum ve bağlamak için 2 adet metot override ediyorum.

    //1.1) menüyü koda bağlayacağımız metotu oluşturduk.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //1.2) Menümüzü layouta bağlamak için inflate'i kullandık. Menu için özel inflate var ve bunu kullandık (MenuInflater).
        MenuInflater menuInflater = getMenuInflater();
        //1.3) İlk önce oluşturduğumuz art_menu yü daha sonra bize bağlanması için onCreateOptionsMenu içerisinde verilen menu'yü inflate içine yazıyoruz.
        menuInflater.inflate(R.menu.art_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //1.4) Oluşturduğum menüye tıklandığında ne olacağını yazdığım override metodu.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //1.5) Eğer benim menümde birden fazla seçenek olursa hepsini if komutuyla yazıyorum. (Eğer herhangi bir seçenek seçilirse onu yap gibi).
        //1.6) Eğer id'si add_art olan item seçilirse beni ArtActivity'e götür komutunu yazıyorum.
        if (item.getItemId() == R.id.add_art){

            //1.7) İntent kullanarak iki sınıf arasında geçiş sağlıyorum.
            Intent intent = new Intent(this,ArtActivity.class);

            //9)MainActivityde iken yeni bir art mı ekleyeceğim yoksa kaydettiğim olana mı gideceğimi ayırt etmek için putextra kullanacağız.
            //9.1) ilk önce yeni bir art oluşturmak için put extra içerisine info yu new değeri ile yolayacağım. Daha sonra artAdapter'e gidip onBindViewHolderın altına kaydedilen veri için kod yazacağım
            intent.putExtra("info", "new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}