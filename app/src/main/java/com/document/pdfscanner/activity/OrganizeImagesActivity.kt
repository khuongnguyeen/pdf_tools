@file:Suppress("DEPRECATION")

package com.document.pdfscanner.activity

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.document.pdfscanner.App
import com.document.pdfscanner.R
import com.document.pdfscanner.adapter.OrganizeImagesAdapter
import com.document.pdfscanner.model.ImagePage
import com.document.pdfscanner.ulti.Action
import com.document.pdfscanner.ulti.ConstantSPKey
import com.document.pdfscanner.ulti.Utils
import kotlinx.android.synthetic.main.activity_organize_images.*

@Suppress("DEPRECATION")
class OrganizeImagesActivity : BaseActivity() {
    internal val TAG = OrganizeImagesActivity::class.java.simpleName
    lateinit var adapter: OrganizeImagesAdapter
    private var closeMoreInfo: OnClickListener = OnClickListener {
        info_tap_more_options.visibility = GONE
        info_tap_more_options.animate()
            .translationY((-info_tap_more_options.height).toFloat())
            .alpha(0.0f).setListener(object : AnimatorListener {
                override fun onAnimationCancel(animator: Animator) {}

                override fun onAnimationRepeat(animator: Animator) {}

                override fun onAnimationStart(animator: Animator) {}

                override fun onAnimationEnd(animator: Animator) {
                    info_tap_more_options.visibility = GONE
                    val edit = sharedPreferences.edit()
                    edit.putBoolean(ORGANIZE_PAGES_TIP, false)
                    edit.apply()
                }
            })
    }
    var imagePages: ArrayList<ImagePage> = ArrayList()
    lateinit var imageUris: ArrayList<String>
    lateinit var sharedPreferences: SharedPreferences
    lateinit var sharedPreferences2: SharedPreferences
    private var showOrganizePagesTip: Boolean = false

    @Suppress("DEPRECATION")
    @SuppressLint("StaticFieldLeak")
    inner class LoadPageThumbnails(arrayList: ArrayList<String>) : AsyncTask<Void, Void, Void>() {
        init {
            imageUris = arrayList
        }

        public override fun onPreExecute() {
            super.onPreExecute()
        }

        public override fun doInBackground(vararg voidArr: Void): Void? {
            var i = 0
            while (i < imageUris.size) {
                val i2 = i + 1
                imagePages.add(
                    ImagePage(
                        i2,
                        imageUris[i]
                    )
                )
                i = i2
            }
            adapter = OrganizeImagesAdapter(
                this@OrganizeImagesActivity,
                imagePages
            )
            return null
        }

        @SuppressLint("RestrictedApi")
        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            recycler_view_organize_pages.layoutManager = GridLayoutManager(
                this@OrganizeImagesActivity,
                3,
                RecyclerView.VERTICAL,
                false
            )
            progress_bar_organize_pages.visibility = GONE
            recycler_view_organize_pages.adapter = adapter
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(15, 0) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val adapterPosition = viewHolder.adapterPosition
                    val adapterPosition2 = target.adapterPosition
                    imagePages.add(
                        adapterPosition,
                        imagePages.removeAt(adapterPosition2)
                    )
                    adapter.notifyItemMoved(
                        adapterPosition2,
                        adapterPosition
                    )
                    val str = TAG
                    val sb = StringBuilder()
                    sb.append("moved from ")
                    sb.append(adapterPosition)
                    sb.append(" to position ")
                    sb.append(adapterPosition2)
                    Log.d(str, sb.toString())
                    return true
                }


                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    val str = this@OrganizeImagesActivity.TAG
                    val sb = StringBuilder()
                    sb.append("Page order after swap ")
                    val organizeImagesActivity = this@OrganizeImagesActivity
                    sb.append(
                        organizeImagesActivity.getOrganizedPages(organizeImagesActivity.imagePages)
                            .toString()
                    )
                    Log.d(str, sb.toString())
                }
            }).attachToRecyclerView(recycler_view_organize_pages)
        }
    }

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, p1 ->
        when {
            p1.equals(ConstantSPKey.VALUE_COLOR) || p1.equals(ConstantSPKey.NIGHT_MODE) -> {
                //  setBackground()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organize_images)
        setSupportActionBar(toolbar_organize_pages)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        sharedPreferences2 = PreferenceManager.getDefaultSharedPreferences(this)
        showOrganizePagesTip = this.sharedPreferences2.getBoolean(ORGANIZE_PAGES_TIP, true)
        info_close!!.setOnClickListener(this.closeMoreInfo)
        //  setBackground()
        sharedPreferences =
            getSharedPreferences(ConstantSPKey.ACTIVITY_SETTING_KEY, Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        this.imageUris = intent.getStringArrayListExtra(Action.IMAGE_URIS)!!
        if (this.showOrganizePagesTip) {
            info_tap_more_options.visibility = View.VISIBLE
        } else {
            info_tap_more_options.visibility = GONE
        }
        LoadPageThumbnails(imageUris).execute()
    }


    fun getOrganizedPages(list: List<ImagePage>): List<Int> {
        val arrayList = ArrayList<Int>()
        for (i in list.indices) {
            arrayList.add(Integer.valueOf((list[i]).pageNumber))
        }
        return arrayList
    }


    private fun showFileNameDialog() {
        val builder = AlertDialog.Builder(this)
        val sb = StringBuilder()
        sb.append("Image_PDF_")
        sb.append(System.currentTimeMillis())
        val sb2 = sb.toString()
        val f = resources.displayMetrics.density
        val editText = EditText(this)
        editText.setText(sb2)
        editText.setSelectAllOnFocus(true)
        builder.setTitle(R.string.enter_file_name)
            .setPositiveButton(R.string.ok, null as DialogInterface.OnClickListener?)
            .setNegativeButton(R.string.cancel, null as DialogInterface.OnClickListener?)
        val create = builder.create()
        val i = (24.0f * f).toInt()
        create.setView(editText, i, (8.0f * f).toInt(), i, (f * 5.0f).toInt())
        create.show()
        create.getButton(-1).setOnClickListener {
            val obj = editText.text.toString()
            if (Utils.isFileNameValid(obj)) {

                            create.dismiss()
                            val bundle = Bundle()
                            bundle.putSerializable(Action.BD_LST, imagePages)
                            bundle.putString(Action.BD_NN, obj)

                            startActivity(Intent(this@OrganizeImagesActivity, ConvertPdf::class.java)
                                .putExtra("key", Action.IMG_TO_PDF)
                                .putExtra(Action.IT_BD, bundle)
                            )

            } else
                editText.error = this@OrganizeImagesActivity.getString(R.string.invalid_file_name)
        }
    }

    companion object {
        var ORGANIZE_PAGES_TIP = "prefs_organize_pages"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.action_check -> {
                if (imagePages.size >= 1) {
                    showFileNameDialog()
                } else {
                    Utils.showDialogSweet(
                        this,
                        SweetAlertDialog.WARNING_TYPE,
                        getString(R.string.select_at_least_one_image)
                    )
                }
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tool, menu)
        return true
    }


}
