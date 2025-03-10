package com.github.libretube.adapters

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.github.libretube.*
import com.github.libretube.obj.PlaylistId
import com.github.libretube.obj.Playlists
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


class PlaylistsAdapter(private val playlists: MutableList<Playlists>, private val activity: Activity): RecyclerView.Adapter<PlaylistsViewHolder>() {
    val TAG = "PlaylistsAdapter"
    override fun getItemCount(): Int {
        return playlists.size
    }
    fun updateItems(newItems: List<Playlists>){
        playlists.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cell = layoutInflater.inflate(R.layout.playlists_row,parent,false)
        return PlaylistsViewHolder(cell)
    }

    override fun onBindViewHolder(holder: PlaylistsViewHolder, position: Int) {
        val playlist = playlists[position]
        val thumbnailImage = holder.v.findViewById<ImageView>(R.id.playlist_thumbnail)
        Picasso.get().load(playlist.thumbnail).into(thumbnailImage)
        holder.v.findViewById<TextView>(R.id.playlist_title).text = playlist.name
        holder.v.findViewById<ImageView>(R.id.delete_playlist).setOnClickListener {
            val builder = AlertDialog.Builder(holder.v.context)
            builder.setTitle(R.string.deletePlaylist)
            builder.setMessage(R.string.areYouSure)
            builder.setPositiveButton(R.string.yes) { dialog, which ->
                val sharedPref = holder.v.context.getSharedPreferences("token", Context.MODE_PRIVATE)
                val token = sharedPref?.getString("token","")!!
                deletePlaylist(playlist.id!!, token, position)
            }
            builder.setNegativeButton(R.string.cancel) { dialog, which ->
            }
            builder.show()
        }
        holder.v.setOnClickListener {
            //playlists clicked
            val activity = holder.v.context as MainActivity
            val bundle = bundleOf("playlist_id" to playlist.id)
            activity.navController.navigate(R.id.playlistFragment,bundle)
        }

    }
    private fun deletePlaylist(id: String, token: String, position: Int) {
        fun run() {
            GlobalScope.launch{
                val response = try {
                    RetrofitInstance.api.deletePlaylist(token, PlaylistId(id))
                }catch(e: IOException) {
                    println(e)
                    Log.e(TAG, "IOException, you might not have internet connection")
                    return@launch
                } catch (e: HttpException) {
                    Log.e(TAG, "HttpException, unexpected response")
                    return@launch
                }finally {

                }
                try{
                    if(response.message == "ok"){
                        Log.d(TAG,"deleted!")
                        playlists.removeAt(position)
                        // FIXME: This needs to run on UI thread?
                        activity.runOnUiThread { notifyDataSetChanged() }

                        /*if(playlists.isEmpty()){
                            view.findViewById<ImageView>(R.id.boogh2).visibility=View.VISIBLE
                        }*/
                    }
                }catch (e:Exception){
                    Log.e(TAG,e.toString())
                }

            }
        }
        run()

    }

}
class PlaylistsViewHolder(val v: View): RecyclerView.ViewHolder(v){
    init {
    }
}
