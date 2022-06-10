package com.juhnny.tpsmartplace.activities

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.get
import com.google.android.material.tabs.TabLayout
import com.juhnny.tpsmartplace.R
import com.juhnny.tpsmartplace.databinding.ActivityMainBinding
import com.juhnny.tpsmartplace.databinding.QuickListBinding
import com.juhnny.tpsmartplace.fragments.PlaceListFragment
import com.juhnny.tpsmartplace.fragments.PlaceMapFragment

class MainActivity : AppCompatActivity() {

    val b by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val quick by lazy { QuickListBinding.inflate(layoutInflater) }

    // 1. 검색 장소 키워드
    var searchQuery:String = "화장실" //초기 검색어 - 내 주변 개방 화장실

    // 2. 현재 내 위치 정보 객체(위도, 경도 정보를 멤버로 보유)
    var mylocation : Location? = null

    // TODO
    // [Google Fused Location API 사용 : play-services-location ]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        //툴바를 제목줄로 설정
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //첫 실행될 프래그먼트를 동적으로 추가
        //첫 시작은 PlaceListFragment가 보여지게
        supportFragmentManager.beginTransaction().add(R.id.container_fragment, PlaceListFragment()).commit()

        b.layoutTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            //기존 탭 외의 탭을 누르면 onTabSelected도 발동하고 onTabUnselected도 발동한다.
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab?.text == "LIST"){
                    supportFragmentManager.beginTransaction().replace(R.id.container_fragment, PlaceListFragment()).commit()
                } else if(tab == b.layoutTab.getTabAt(1)){
                    supportFragmentManager.beginTransaction().replace(R.id.container_fragment, PlaceMapFragment()).commit()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        b.etSearch.setOnEditorActionListener { textView, i, keyEvent ->
            searchQuery = b.etSearch.text.toString()
            //카카오 장소 검색 API 작업
            searchPlace()

            false //이벤트 소모를 끝내지 않겠다. -> 다른 이어지는 작업들이 있다면 하게 두겠다.
        }

        setQuickSelectListener()
    }

    //카카오 키워드 장소검색 API 수행하는 기능메소드
    private fun searchPlace(){
        Toast.makeText(this, "$searchQuery", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val m = menuInflater.inflate(R.menu.actionbar_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setQuickSelectListener(){
        b.layoutQuick.quickToilet.setOnClickListener { clickQuickSelect(it) }
        b.layoutQuick.quickGas.setOnClickListener { clickQuickSelect(it) }
        b.layoutQuick.quickEv.setOnClickListener { clickQuickSelect(it) }
        b.layoutQuick.quickMovie.setOnClickListener { clickQuickSelect(it) }
        b.layoutQuick.quickPark.setOnClickListener { clickQuickSelect(it) }
        b.layoutQuick.quickRestaurant.setOnClickListener { clickQuickSelect(it) }
        b.layoutQuick.quickA.setOnClickListener { clickQuickSelect(it) }
        b.layoutQuick.quickB.setOnClickListener { clickQuickSelect(it) }
        b.layoutQuick.quickC.setOnClickListener { clickQuickSelect(it) }
    }

    //선택됐던 QuickSelect의 id를 저장. 다른 게 선택됐을 때 배경을 바꿔주기 위함
    var selectedQuickSelectId:Int = R.id.quick_toilet //기본값은 화장실

    private fun clickQuickSelect(view:View){
        when(view.id){
            b.layoutQuick.quickToilet.id -> { searchQuery = "화장실" }
            b.layoutQuick.quickGas.id -> { searchQuery = "주유소" }
            b.layoutQuick.quickEv.id -> { searchQuery = "전기차충전소" }
            b.layoutQuick.quickMovie.id -> { searchQuery = "영화관" }
            b.layoutQuick.quickPark.id -> { searchQuery = "공원" }
            b.layoutQuick.quickRestaurant.id -> { searchQuery = "식당" }
            b.layoutQuick.quickA.id -> { searchQuery = "약국" }
            b.layoutQuick.quickB.id -> { searchQuery = "편의점" }
            b.layoutQuick.quickC.id -> { searchQuery = "카페" }
        }
        searchPlace()

        b.etSearch.text.clear() //글자를 지우게 하면 포커스도 그 쪽으로 이동되는데 그걸 막으려면
        b.etSearch.clearFocus()


        findViewById<View>(selectedQuickSelectId).setBackgroundResource(R.drawable.bg_quick_list)
        selectedQuickSelectId = view.id
        view.setBackgroundResource(R.drawable.bg_quick_list_selected) //얘가 나중에 와야 같은 걸 재선택했을 때도 색이 지워지지 않음
    }
}