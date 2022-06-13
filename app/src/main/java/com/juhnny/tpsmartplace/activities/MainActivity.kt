package com.juhnny.tpsmartplace.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.get
import com.google.android.gms.location.*
import com.google.android.material.tabs.TabLayout
import com.juhnny.tpsmartplace.R
import com.juhnny.tpsmartplace.databinding.ActivityMainBinding
import com.juhnny.tpsmartplace.databinding.QuickListBinding
import com.juhnny.tpsmartplace.fragments.PlaceListFragment
import com.juhnny.tpsmartplace.fragments.PlaceMapFragment
import com.juhnny.tpsmartplace.network.RetrofitHelper
import com.juhnny.tpsmartplace.network.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    val b by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val quick by lazy { QuickListBinding.inflate(layoutInflater) }

    // 1. 검색 장소 키워드
    var searchQuery:String = "화장실" //초기 검색어 - 내 주변 개방 화장실

    // 2. 현재 내 위치 정보 객체(위도, 경도 정보를 멤버로 보유)
    var mylocation : Location? = null

    // [Google Fused Location API 사용 : play-services-location ]
    //안드로이드에도 기본적으로 LocationManager가 있지만 좀 더 복잡하고 부정확하다고
    val providerClient : FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) }


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

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        b.etSearch.setOnEditorActionListener { textView, i, keyEvent ->
            searchQuery = b.etSearch.text.toString()
            //카카오 장소 검색 API 작업
            searchPlace()

            false //이벤트 소모를 끝내지 않겠다. -> 다른 이어지는 작업들이 있다면 하게 두겠다.
        }

        setQuickSelectListener() //QuickSelect 버튼들 작동 설정

        val permissions:Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if( checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_DENIED){
            requestPermissions(permissions, 111) //다이얼로그를 띄워서 허용을 받는..
        } else {
            Toast.makeText(this, "퍼미션 허용됨", Toast.LENGTH_SHORT).show()
            //내 위치탐색 요청하는 기능 호출.
            requestMyLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) requestMyLocation()
        else Toast.makeText(this, "내 위치정보를 허용하지 않아 검색기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
    }

    //내 위치정보 얻어오는 기능 코드
    private fun requestMyLocation(){
        //lastLocation()은 마지막에 썼던 좌표를 가져오기 때문에 실시간 위치정보 갱신을 요청

        //위치 요청에 대한 옵션을 설정하는 객체
        val locationRequest:LocationRequest = LocationRequest.create()
        locationRequest.interval = 1000 //1초마다. 대충 해도 어차피 한번만 사용하고 끌 거라 괜찮
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY //높은 정확도 우선
        //위치 업데이트 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            //FINE_LOCATION, COARSE_LOCATION 둘 다 거부 상태이면
            return
        }

        //위에서 만든 LocationRequest와 아래에서 만든 LocationCallback을 사용
        providerClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        //permission을 처리해줘야만 사용 가능. 자동완성을 쓰면 위와 같이 나옴
    }

    //위치정보 요청 결과에 대한 콜백
    private val locationCallback:LocationCallback = object : LocationCallback(){ //추상클래스는 생성자 괄호 필요
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            //갱신된 LocationResult 객체에게 위치정보 가져오기
            mylocation = p0.lastLocation
            //위치 탐색이 더 필요없으니 위치정보 업데이트 종료시키기
            providerClient.removeLocationUpdates(this)
            //내 위치 정보를 얻었으니 카카오 검색을 시작
            searchPlace()
        }
    }

    //카카오 키워드 장소검색 API 수행하는 기능메소드
    private fun searchPlace(){
        Toast.makeText(this, "$searchQuery, 내 위치: ${mylocation?.latitude}, ${mylocation?.longitude}", Toast.LENGTH_LONG).show()

        //레트로핏을 이용하여 카카오 키워드 장소검색 API를 파싱하기
        val retrofit = RetrofitHelper.getRetrofitInstance("https://dapi.kakao.com")
        retrofit.create(RetrofitService::class.java)
            .searchPlaceToString(searchQuery, mylocation?.latitude.toString(), mylocation?.longitude.toString())
            .enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                val result:String? = response.body()
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(result.toString())
                    .show()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@MainActivity, "서버 오류가 있습니다.\n잠시 뒤에 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
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