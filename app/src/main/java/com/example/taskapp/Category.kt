package com.example.taskapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import java.util.*

class Category : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)



        category_edit_text .setOnClickListener(mCategoryClickListener)
    }




    private val mCategoryClickListener = View.OnClickListener () {
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()





}
}
