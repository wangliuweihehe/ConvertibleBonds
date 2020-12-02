package com.wlw.stock

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.wlw.stock.adapter.MainListAdapter
import com.wlw.stock.http.JsonCallback
import com.wlw.stock.model.DatadBean
import com.wlw.stock.model.MainDataBean
import com.wlw.stock.utils.ClipBoardUtils
import com.wlw.stock.utils.Constant
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gyf.immersionbar.ImmersionBar
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.default_toolbar.*
import java.lang.Exception
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {
    private val webUrl = "https://www.jisilu.cn/data/cbnew/#pre"
    private val url = "https://www.jisilu.cn/data/cbnew/pre_list/?___jsl=LST___t="
    private lateinit var mAdapter: MainListAdapter

    private val mData = arrayListOf<MainDataBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ImmersionBar.with(this).titleBar(toolbar).statusBarDarkFont(true).init()

        tvTitle.text = "待发行可转债"

        ivBack.setOnClickListener { finish() }
        ivSave.setOnClickListener {
            requestPermission()
        }
        initRecyclerView()
        requestData()
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = MainListAdapter()
        recyclerView.adapter = mAdapter

        mAdapter.addChildClickViewIds(R.id.tv_code)
        mAdapter.setOnItemChildClickListener { _, _, position ->
            val code = mAdapter.getItem(position).code
            ClipBoardUtils.copy(this@MainActivity, code, "复制成功 $code")
            saveCodes(code)
        }
        getCodes()
    }

    private fun requestData() {
        OkGo.get<DatadBean>(url + System.currentTimeMillis())
            .tag(this)
            .execute(object : JsonCallback<DatadBean>() {
                override fun onSuccess(response: Response<DatadBean>) {
                    initData(response.body())
                }

                override fun onError(response: Response<DatadBean>?) {
                    super.onError(response)
                    ToastUtils.showShort("请求接口出错")
                }
            })
    }

    private fun initData(dataBean: DatadBean) {
        for (row in dataBean.rows) {
            val cell = row.cell
            if (TextUtils.equals(cell.cb_type, "可转债")) {
                if (cell.progress_nm.contains("证监会") || TextUtils.equals(
                        "发审委通过",
                        cell.progress_nm
                    )
                ) {
                    val bean = MainDataBean()
                    bean.code = cell.stock_id
                    bean.name = cell.stock_nm
                    bean.equity = cell.cb_amount
                    bean.price = cell.price
                    bean.progress = cell.progress_nm
                    bean.progressDate = cell.progress_dt

                    val number: Int = if (cell.stock_id.startsWith("60")) {
                        (ceil((1000.toDouble() / bean.equity.toDouble() / cell.price.toDouble() / 2)) * 100).toInt()
                    } else {
                        (ceil((1000.toDouble() / bean.equity.toDouble() / cell.price.toDouble())) * 100).toInt()
                    }
                    bean.number = number.toString()
                    mData.add(bean)
                } else {
                    if (!TextUtils.isEmpty(cell.apply_date) && afterToday(cell.apply_date)) {
                        val bean = MainDataBean()
                        cell.apply_date
                        bean.code = cell.stock_id
                        bean.name = cell.stock_nm
                        bean.equity = cell.cb_amount
                        bean.price = cell.price
                        bean.progress = cell.progress_nm
                        bean.progressDate = cell.progress_dt
                        val number: Int = if (cell.stock_id.startsWith("60")) {
                            (ceil((1000.toDouble() / bean.equity.toDouble() / cell.price.toDouble() / 2)) * 100).toInt()
                        } else {
                            (ceil((1000.toDouble() / bean.equity.toDouble() / cell.price.toDouble())) * 100).toInt()
                        }
                        bean.number = number.toString()
                        mData.add(bean)
                    }
                }
            }
        }
        mData.sort()
        mAdapter.setNewInstance(mData)
    }

    @SuppressLint("SimpleDateFormat")
    private fun afterToday(date: String): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd")
        var isAfter = false
        try {
            val parse = format.parse(date)
            isAfter = parse.time >= System.currentTimeMillis()
        } catch (e: Exception) {
            LogUtils.e(e.message)
            e.printStackTrace()
        }
        return isAfter
    }

    @SuppressLint("WrongConstant")
    private fun requestPermission() {
        PermissionUtils.permission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
            .callback(object : PermissionUtils.SimpleCallback {
                override fun onGranted() {
                    saveExcel()
                }

                override fun onDenied() {

                }
            })
            .request()
    }

    private fun saveExcel() {

    }


    private fun saveCodes(code: String) {
        if (TextUtils.isEmpty(code)) {
            return
        }
        val result = SPUtils.getInstance().getString(Constant.CEDES)
        val codes: MutableList<String>
        codes = if (TextUtils.isEmpty(result)) {
            ArrayList()
        } else {
            Gson().fromJson(
                result,
                object : TypeToken<List<String?>?>() {}.type
            )
        }

        if (codes.contains(code)) {
            codes.remove(code)
        } else {
            codes.add(code)
        }

        mAdapter.codes = codes
        SPUtils.getInstance().put(Constant.CEDES, Gson().toJson(codes))
    }

    private fun getCodes() {
        val result = SPUtils.getInstance().getString(Constant.CEDES)
        val codes: List<String>
        codes = if (TextUtils.isEmpty(result)) {
            ArrayList()
        } else {
            Gson().fromJson(result, object : TypeToken<List<String?>?>() {}.type)
        }
        mAdapter.codes = codes
    }
}