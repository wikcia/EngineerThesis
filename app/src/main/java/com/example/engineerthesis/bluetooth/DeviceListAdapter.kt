package com.example.engineerthesis.bluetooth

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.engineerthesis.R

class DeviceListAdapter(private val context: Context, private val deviceList: List<Any>) :
    RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textViewDeviceName)
        val textAddress: TextView = itemView.findViewById(R.id.textViewDeviceAddress)
        val imageViewIcon: ImageView = itemView.findViewById(R.id.imageViewIcon)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayoutDeviceInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_info_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemHolder = holder
        val deviceInfoModel = deviceList[position] as DeviceInfoModel
        itemHolder.textName.text = deviceInfoModel.deviceName
        itemHolder.textAddress.text = deviceInfoModel.deviceHardwareAddress
        itemHolder.imageViewIcon.setImageResource(R.drawable.baseline_broadcast_on_personal_24)

        // When a device is selected
        itemHolder.linearLayout.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            // Send device details to the MainActivity
            intent.putExtra("deviceName", deviceInfoModel.deviceName)
            intent.putExtra("deviceAddress", deviceInfoModel.deviceHardwareAddress)
            // Call MainActivity
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }
}
