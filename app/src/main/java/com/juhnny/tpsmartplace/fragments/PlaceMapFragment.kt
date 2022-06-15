package com.juhnny.tpsmartplace.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.juhnny.tpsmartplace.activities.MainActivity
import com.juhnny.tpsmartplace.activities.PlaceUrlActivity
import com.juhnny.tpsmartplace.databinding.FragmentPlaceListBinding
import com.juhnny.tpsmartplace.databinding.FragmentPlaceMapBinding
import com.juhnny.tpsmartplace.model.Place
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class PlaceMapFragment : Fragment() {

    val b by lazy { FragmentPlaceMapBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return b.root
    }

    val mapView by lazy { MapView(context) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //맵뷰 컨테이너에 맵뷰 객체를 추가
        b.containerMapview.addView(mapView)
        //하지만 오류. ARM 대응 라이브러리라서 윈도우 에뮬레이터에서는 돌아가지 않는다. 폰이나 맥에서는 된다고..

        // 지도관련 설정 ( 지도위치, 마커 추가 등...)
        setMapAndMarkers()
    }

    private fun setMapAndMarkers(){

        //마커나 말풍선의 클릭이벤트에 반응하는 리스너 등록
        //** 반드시 마커 추가하는 작업보다 먼저 등록되어 있어야 동작함 - 개발문서엔 없는 내용. **
        mapView.setPOIItemEventListener(markerEventListener)

        // 지도 중심좌표 설정
        // 현재 내 위치로 이동
        val lat:Double = (activity as MainActivity).mylocation?.latitude ?: 37.5666805
        val lng:Double = (activity as MainActivity).mylocation?.longitude ?: 126.9784147
        //중심점 변경 + 줌 레벨 변경
        val myMapPoint = MapPoint.mapPointWithGeoCoord(lat, lng)
        mapView.setMapCenterPointAndZoomLevel(myMapPoint, 9, true)
        mapView.zoomIn(true)
//        mapView.zoomOut(true)

        //내 위치 마커 추가
        val marker = MapPOIItem()
        marker.apply {
            this.itemName = "ME"
            this.mapPoint = myMapPoint
            this.markerType = MapPOIItem.MarkerType.BluePin
            this.selectedMarkerType = MapPOIItem.MarkerType.YellowPin
        }
        mapView.addPOIItem(marker)

        //검색결과 장소들 마커 추가
        val documents = (activity as MainActivity).searchPlaceResponse?.documents
        documents?.forEach {
            //documents가 null이면 forEach문은 아예 동작하지 않음. for문이었으면 조건문으로 처리했을 부분이 좀 더 간단해짐
            val point:MapPoint = MapPoint.mapPointWithGeoCoord(it.y.toDouble(), it.x.toDouble())

            //apply 함수는 인스턴스를 새로 생성하고 특정 변수에 할당하기 전에 초기화작업을 해줄 수 있는 스코프를 만들어준다.
            //apply 함수 내의 모든 명령이 수행되고 나면 명령들이 적용되어 새로 생성된 인스턴스를 반환한다.
            val marker:MapPOIItem = MapPOIItem().apply {
                itemName = it.place_name
                mapPoint = point
                markerType = MapPOIItem.MarkerType.RedPin
                selectedMarkerType = MapPOIItem.MarkerType.YellowPin
                //해당 POI item(마커)와 관련된 정보를 추가적으로 저장하기 위한 별도 주머니 같은 객체(타입은 Any)
                userObject = it
            }
            //맵에 마커 추가
            mapView.addPOIItem(marker)
        }

    }

    //마커나 말풍선이 클릭되는 이벤트에 반응하는 리스터 객체
    private val markerEventListener:MapView.POIItemEventListener = object : MapView.POIItemEventListener{
        override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
            //마커가 클릭됐을 때 발동하는 메소드
        }

        override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
            //deprecated. 이제는 아래 오버로딩된 메소드 사용 권장
        }

        override fun onCalloutBalloonOfPOIItemTouched(
            p0: MapView?,
            p1: MapPOIItem?,
            p2: MapPOIItem.CalloutBalloonButtonType?
        ) {
            //마커의 말풍선을 클릭했을 때 발동하는 메소드
            //두번째 파라미터 p1: 마커 객체
            if(p1?.userObject == null) return
            val place:Place = p1.userObject as Place
            //장소 상세정보 보여주는 화면으로 이동
            val intent = Intent(requireContext(), PlaceUrlActivity::class.java)
            intent.putExtra("placeUrl", place.place_url)
            startActivity(intent)
        }

        override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
            //마커를 드래그하여 움직였을 때 발동
        }
    }//listener
}


//혹시 몰라 복사해놓는 강의 코드
//class PlaceMapFragment : Fragment() {
//
//    val binding: FragmentPlaceMapBinding by lazy { FragmentPlaceMapBinding.inflate(layoutInflater) }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return binding.root
//    }
//
//    // 카카오지도는 AVD에서는 테스트 안됨. ( Mac M1 사용자는 가능함 )
//    val mapView: MapView by lazy { MapView(context) }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        //맵뷰 컨테이너에 맵뷰 객체를 추가
//        binding.containerMapview.addView(mapView)
//
//        // 지도관련 설정 ( 지도위치, 마커 추가 등...)
//        setMapAndMarkers()
//    }
//
//    private fun setMapAndMarkers(){
//
//        // 마카 or 맘풍선의 클릭이벤트에 반응하는 리스너 등록
//        // ** 반드시 마커 추가하는 것보다 먼저 등록되어 있어야 동작함. **
//        mapView.setPOIItemEventListener(markerEventListener)
//
//
//        // 지도 중심좌표 설정
//        // 현재 내 위치로 이동
//        var lat: Double = (activity as MainActivity).mylocation?.latitude ?: 37.5666805
//        var lng: Double = (activity as MainActivity).mylocation?.longitude ?: 126.9784147
//
//        // 위도/경도를 카카오지도의 맴좌표객체(MapPoint)로 생성
//        var myMapPoint: MapPoint = MapPoint.mapPointWithGeoCoord(lat, lng)
//        mapView.setMapCenterPointAndZoomLevel(myMapPoint, 5, true)
//        mapView.zoomIn(true)
//        mapView.zoomOut(true)
//
//        // 내 위치 마커 추가
//        val marker = MapPOIItem()
//        //마커 설정들
//        marker.apply {
//            //this.itemName= "ME"
//            //this.은 생략가능..
//            itemName="ME"
//            mapPoint= myMapPoint
//            markerType= MapPOIItem.MarkerType.BluePin
//            selectedMarkerType= MapPOIItem.MarkerType.YellowPin
//        }
//        mapView.addPOIItem(marker)
//
//        // 검색결과 장소들 마커 추가
//        val documents: MutableList<Place>? = (activity as MainActivity).searchPlaceResponse?.documents
//        documents?.forEach {
//            val point: MapPoint = MapPoint.mapPointWithGeoCoord(it.y.toDouble(), it.x.toDouble())
//
//            //마커객체 생성
//            var marker: MapPOIItem= MapPOIItem().apply {
//                itemName= it.place_name
//                mapPoint= point
//                markerType= MapPOIItem.MarkerType.RedPin
//                selectedMarkerType= MapPOIItem.MarkerType.YellowPin
//                // 해당 POI item(마커)와 관련된 정보를 저장하고 있는 데이터객체를 보관
//                userObject= it
//            }
//            mapView.addPOIItem(marker)
//        }///////////////////////////////////
//
//    }// method..
//
//    //마커나 말풍선이 클릭되는 이벤트에 반응하는 리스너객체  ////////
//    private val markerEventListener: MapView.POIItemEventListener= object : MapView.POIItemEventListener{
//        override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
//            //마커 클릭되었을때 발동하는 메소드
//        }
//
//        override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
//            // deprecated .. 이제는 아래 오버로딩된 메소드 사용 권장
//        }
//
//        override fun onCalloutBalloonOfPOIItemTouched(
//            p0: MapView?,
//            p1: MapPOIItem?,
//            p2: MapPOIItem.CalloutBalloonButtonType?
//        ) {
//            // 마커의 말풍선을 클릭했을때 발동하는 메소드
//            // 두번째 파라미터 p1 : 마커객체
//            if( p1?.userObject == null ) return
//
//            val place: Place= p1?.userObject as Place
//
//            // 장소 상세정보 보여주는 화면으로 이동
//            val intent= Intent(context, PlaceUrlActivity::class.java)
//            intent.putExtra("place_url", place.place_url)
//            startActivity(intent)
//        }
//
//        override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
//            // 마커를 드래그하여 움직였을때 발동
//        }
//    }
//
//
//}// Fragment class...