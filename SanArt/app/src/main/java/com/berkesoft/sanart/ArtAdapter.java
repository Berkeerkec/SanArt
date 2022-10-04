package com.berkesoft.sanart;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.berkesoft.sanart.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {

    ArrayList<Art> artArrayList;

    public ArtAdapter(ArrayList<Art> artArrayList){
        this.artArrayList = artArrayList;
    }



    @NonNull
    //8.3)Burada layouta bağlama işlemi yapıyoruz.
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //8.3.1) Burada bindingi layouta infilate işlemi yaparak bağlıyoruz.
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ArtHolder(recyclerRowBinding);
    }

    //8.4) Artık holderım hazır. Burada oluşturduğum xmlde isimi gösterebileceğim. MainActivitydeki RecyclerView'a bağlayabilirim artık.
    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText(artArrayList.get(position).name);

        //9.2) MainActivityde yaptığım gibi burada da item'a tıklanınca info'yu göndereceğim değerim ise old seçtim.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), ArtActivity.class);
                intent.putExtra("info", "old");
                intent.putExtra("artId", artArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);

            }
        });
    }

    //8.2) Kaç tane oluşturulacaksa buraya yazıyoruz.
    //8.2.1) artArrayList içerisinde ne kadar varsa o kadar döndürülsün diye return'e artArrayList.size(); yazıyorum.
    @Override
    public int getItemCount() {

        return artArrayList.size();
    }

    //8)Adaptörümün içine görünüm tutucu bir sınıf yazıyorum.
    //8.1) ViewHolder oluşturduktan sonra xml oluşturmamız lazım.
    public class ArtHolder extends RecyclerView.ViewHolder{

        private RecyclerRowBinding binding;
        //8.1.1) ArtHolderın içine RecyclerRowBinding binding yazıyoruz ve private edip eşitliyoruz. Burada bir tutucu oluşturduk.
        //8.1.2) Görünüm tutucumu oluşturduktan sonra ArtAdapter sınıfındaki RecyclerView.Adapter<> içerisine ArtAdapter.ArtHolder yazıyorum ve implement(alt enter) yapıyorum
        public ArtHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
