package com.musazenbilci.recipebook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recyler_row.view.*

class ListRecyclerAdapter(val yemekListesi:ArrayList<String>,val idListesi:ArrayList<Int>):RecyclerView.Adapter<ListRecyclerAdapter.RecipeHolderVH>() {
    class RecipeHolderVH(itemView:View):RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeHolderVH {
        //DEFINE AN INFLATER TO HANDLE THE XML FILE FOR EVERY ROW IN THE RECYCLERVIEW LIST
        val inflater =LayoutInflater.from(parent.context)
        val view=inflater.inflate(R.layout.recyler_row,parent,false)
        return RecipeHolderVH(view)
    }

    override fun onBindViewHolder(holder: RecipeHolderVH, position: Int) {
        holder.itemView.recycler_row_text.text=yemekListesi[position]
        holder.itemView.setOnClickListener {
            //FIRST ARGUMENT INDICATES WE GOT HERE THROUGH THE LIST WHERE THE SECOND INDICATES THE ID OF THE WHİCH ROW WE PRESSED
            val action=ListFragmentDirections.actionListFragmentToRecipeFragment("recyclerdangeldim",idListesi[position])
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return yemekListesi.size
    }
}