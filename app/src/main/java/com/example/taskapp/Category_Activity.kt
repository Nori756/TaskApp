package com.example.taskapp


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_category_.*
import kotlinx.android.synthetic.main.content_input.*
import kotlinx.android.synthetic.main.content_input.category_edit_text
import java.util.*

class Category_Activity : AppCompatActivity() {

    private var mCategory: Category? = null


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_category_)

            // EXTRA_TASK から Task の id を取得して、 id から カテゴリー のインスタンスを取得する
            val intent = intent
            val categoryId = intent.getIntExtra(EXTRA_TASK, -1)
            val realm = Realm.getDefaultInstance()
            mCategory = realm.where(Category::class.java).equalTo("id", categoryId).findFirst()
            realm.close()


            category_button.setOnClickListener(mCategoryClickListener)

            if (mCategory == null) {

                // 更新の場合
                category_edit_text.setText(mCategory!!.category)


            }

        }


        private val mCategoryClickListener = View.OnClickListener () {
            addTask()
            finish()
        }

    private fun addTask() {
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()

        if (mCategory == null) {
            // 新規作成の場合
            mCategory = Category()

            val categoryRealmResults = realm.where(Category::class.java).findAll()

            val identifier: Int =
                if (categoryRealmResults.max("id") != null) {
                    categoryRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mCategory!!.id = identifier
        }

        val category = category_edit_text.text.toString()


        mCategory!!.category = category

        realm.copyToRealmOrUpdate(mCategory!!)
        realm.commitTransaction()

        realm.close()

    }




    }
