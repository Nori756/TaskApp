package com.example.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import io.realm.RealmChangeListener
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.content_input.*
import java.util.*
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter

const val EXTRA_TASK ="com.example.taskapp.TASK"

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    private lateinit var mArrayAdapter: ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ArrayAdapter ArrayAdapter = new ArrayAdapter(this, id, list);


        fab.setOnClickListener { _ ->
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            startActivity(intent)
        }

        // Realmの設定
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        // ListViewの設定
        mTaskAdapter = TaskAdapter(this@MainActivity)

        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK") { _, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        reloadListView()

        val editText = findViewById<View>(R.id.search_edit_text) as EditText



        editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                //テキスト変更前
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //テキスト変更中
            }

            override fun afterTextChanged(s: Editable) {//テキスト変更後


                if (s.toString().equals("")) {

                    // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
                    val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

                    // 上記の結果を、TaskList としてセットする
                    mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

                    // TaskのListView用のアダプタに渡す
                    listView1.adapter = mTaskAdapter

                    // 表示を更新するために、アダプターにデータが変更されたことを知らせる
                    mTaskAdapter.notifyDataSetChanged()
                } else {
                    // Realmデータベースから、「全てのデータを取得して新しいカテゴリーに並べた結果」を取得
                    val taskRealmResults = mRealm.where(Task::class.java).equalTo("category", s.toString()).findAll()
                        .sort("date", Sort.DESCENDING)


                    // 上記の結果を、TaskList としてセットする
                    mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

                    // TaskのListView用のアダプタに渡す
                    listView1.adapter = mTaskAdapter

                    // 表示を更新するために、アダプターにデータが変更されたことを知らせる
                    mTaskAdapter.notifyDataSetChanged()

                }


            }
        })


        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        val categoryRealmResults = mRealm.where(Category::class.java).findAll()

        // 上記の結果を、カテゴリーList としてセットする
        mArrayAdapter.CategoryList= mRealm.copyFromRealm(categoryRealmResults)

        // TaskのListView用のアダプタに渡す
        listView1.adapter = mArrayAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mArrayAdapter.notifyDataSetChanged()


        val spinnerItems = arrayOf(category)

        // ArrayAdapter
        val adapter = ArrayAdapter(applicationContext,
            android.R.layout.simple_spinner_item, spinnerItems)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // spinner に adapter をセット
        // Kotlin Android Extensions
        spinner.adapter = adapter

        // リスナーを登録
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //　アイテムが選択された時
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?, position: Int, id: Long
            ) {
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
                // Kotlin Android Extensions
                textView.text = item
            }

            //　アイテムが選択されなかった
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //

            }
        }
    }

    private fun reloadListView() {
        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }



    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }
}
