package com.musazenbilci.recipebook

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_recipe.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.jar.Manifest


class RecipeFragment : Fragment() {

    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveTheRecipeButton.setOnClickListener {
            saveTheRecipe(it)
        }
        imageView.setOnClickListener {
            takeTheImage(it)
        }
        arguments?.let {
            var gelenbilgi=RecipeFragmentArgs.fromBundle(it).bilgi
            if(gelenbilgi.equals("menudengeldim")){
                //IF WE COME THROUGH MENU ITEM
                recipeNameText.setText("")
                componentsNameText.setText("")
                saveTheRecipeButton.visibility=View.VISIBLE
                invisibleTextView.visibility=View.INVISIBLE
                val gorselSecmeArkaPlan=BitmapFactory.decodeResource(context?.resources,R.drawable.gorsel_secimi)
                imageView.setImageBitmap(gorselSecmeArkaPlan)
            }else{
                //IF WE COME THROUGH RECYCLERVİEW
                saveTheRecipeButton.visibility=View.INVISIBLE
                invisibleTextView.visibility=View.VISIBLE
                invisibleTextView.setText("AFİYET BAL ŞEKER OLSUN")
                val secilenId=RecipeFragmentArgs.fromBundle(it).id
                context?.let {
                    try {
                        val veritabani=it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                        val cursor=veritabani.rawQuery("SELECT * FROM yemekler WHERE id= ?", arrayOf(secilenId.toString()))
                        val yemekIsmiId=cursor.getColumnIndex("yemekismi")
                        val yemekMalzemeId=cursor.getColumnIndex("yemekmalzemesi")
                        val gorselId=cursor.getColumnIndex("gorsel")
                        while (cursor.moveToNext()){
                            recipeNameText.setText(cursor.getString(yemekIsmiId))
                            componentsNameText.setText(cursor.getString(yemekMalzemeId))
                            //CONVERTING AN BYTE ARRAY FROM THE DATABASE TO IMG TO USE IT ON THE RECIPE FRAGMENT
                            val byteDizisi=cursor.getBlob(gorselId)
                            val bitmap=BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                            imageView.setImageBitmap(bitmap)
                        }
                        cursor.close()
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    fun saveTheRecipe(view: View){
        //SQLite data saving
        val recipeName=recipeNameText.text.toString()
        val components=componentsNameText.text.toString()
        if (secilenBitmap!=null){
            //WE RESIZE THE IMAGES TO MAKE SURE THEY ARE NOT LARGER THAN SQLITE CAN HANDLE
            val smallerBitmap=createSmallBitmap(secilenBitmap!!,300)
            //TO BE ABLE TO STORE IMAGES IN THE DATABASE CONVERT THEM TO BYTE ARRAYS BY OUTPUTSTREAM
            val outputStream=ByteArrayOutputStream()
            smallerBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi=outputStream.toByteArray()
            try {
                context?.let {
                    //SQLITE DATABASE CREATION AND INSERTING THE VALUES GİVEN BY USER
                    val database=it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY,yemekismi VARCHAR,yemekmalzemesi VARCHAR,gorsel BLOB )")

                    val sqlString="INSERT INTO yemekler (yemekismi,yemekmalzemesi,gorsel) VALUES (?,?,?)"
                    val statement=database.compileStatement(sqlString)
                    statement.bindString(1,recipeName)
                    statement.bindString(2,components)
                    statement.bindBlob(3,byteDizisi)
                    statement.execute()
                }


            }catch (e:Exception){
                e.printStackTrace()
            }
            //AFTER SAVING THE RECIPE,TAKE THE USER TO THE LISTFRAGMENT IN ORDER TO SEE WHAT THEY JUST SAVED
            val action=RecipeFragmentDirections.actionRecipeFragmentToListFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }
    fun takeTheImage(view: View){
        activity?.let {
            if(ContextCompat.checkSelfPermission(it.applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                //if permission is not granted
                //REQUEST PERMISSION WITH REQUESTCODE 1
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }
            else{
                //if permission is already granted
                //ACTIVITY WITH REQUESTCODE 2
                val galeryIntent =Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeryIntent,2)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //REQUESTCODE 1
        if (requestCode==1){
            if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                val galeryIntent =Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeryIntent,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //REQUESTCODE 2
        if(requestCode==2 && resultCode==Activity.RESULT_OK && data!=null){
            secilenGorsel=data.data
            try {
                context?.let {
                    if(Build.VERSION.SDK_INT >=28){
                        val source=  ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                        secilenBitmap=ImageDecoder.decodeBitmap(source)
                        imageView.setImageBitmap(secilenBitmap)
                    }else{
                        secilenBitmap=MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                        imageView.setImageBitmap(secilenBitmap)
                    }


                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    //BITMAP RESIZER FUNCTION(BASIC)
    fun createSmallBitmap(userBitmap: Bitmap,maximumSize:Int):Bitmap{
        var width=userBitmap.width
        var height=userBitmap.height
        val bitmapProportion=width.toDouble()/height.toDouble()
        if (bitmapProportion>1){
            width=maximumSize
            val smallerHeight=width/bitmapProportion
            height=smallerHeight.toInt()
        }else{
            height=maximumSize
            val smallerWidth=height*bitmapProportion
            width=smallerWidth.toInt()
        }

        return Bitmap.createScaledBitmap(userBitmap,width,height,true)
    }
}