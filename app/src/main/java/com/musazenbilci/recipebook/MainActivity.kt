package com.musazenbilci.recipebook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.Navigation

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater=menuInflater
        menuInflater.inflate(R.menu.add_recipe,menu)

        return super.onCreateOptionsMenu(menu)
    }
    //WHERE MENU ITEM'S JOB IS DEFINED
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.addRecipeItem){
            //FİRST ARGUMENT OF THE ACTION WILL INDICATES WE GET TO THE OTHER FRAGMENT THROUGH MENU BUTTON
            val action=ListFragmentDirections.actionListFragmentToRecipeFragment("menudengeldim",0)
            Navigation.findNavController(this,R.id.fragment).navigate(action)
        }
        return super.onOptionsItemSelected(item)
   }
}