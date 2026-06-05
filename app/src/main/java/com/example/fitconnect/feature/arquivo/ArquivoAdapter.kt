package com.example.fitconnect.feature.arquivo

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class ArquivoAdapter(
    private var lista: MutableList<ArquivoBanco>,
    private val onDeletar: (ArquivoBanco) -> Unit
) : RecyclerView.Adapter<ArquivoAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivTipo: ImageView = view.findViewById(R.id.iv_tipo_arquivo)
        val tvNome: TextView = view.findViewById(R.id.tv_nome_arquivo)
        val tvInfo: TextView = view.findViewById(R.id.tv_info_arquivo)
        val ivDeletar: ImageView = view.findViewById(R.id.iv_deletar_arquivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_arquivo, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val arq = lista[position]
        holder.tvNome.text = arq.nome

        val tamanhoStr = when {
            arq.tamanho_kb <= 0 -> ""
            arq.tamanho_kb >= 1024 -> "${"%.1f".format(arq.tamanho_kb / 1024.0)} MB"
            else -> "${arq.tamanho_kb} KB"
        }
        val dataStr = try {
            val entrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val saida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            saida.format(entrada.parse(arq.criado_em.take(10))!!)
        } catch (e: Exception) { arq.criado_em.take(10) }

        holder.tvInfo.text = if (tamanhoStr.isNotEmpty()) "$tamanhoStr · $dataStr" else dataStr

        holder.ivTipo.setImageResource(
            when (arq.tipo) {
                "video" -> R.drawable.ic_file_video
                "imagem" -> R.drawable.ic_file_image
                else -> R.drawable.ic_file_pdf
            }
        )
        holder.ivDeletar.setOnClickListener { onDeletar(arq) }
        holder.itemView.setOnClickListener {
            if (arq.arquivo_url.isBlank()) return@setOnClickListener
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(arq.arquivo_url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                holder.itemView.context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                android.widget.Toast.makeText(
                    holder.itemView.context,
                    "Nenhum app encontrado para abrir este arquivo.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun getItemCount() = lista.size

    fun atualizar(nova: List<ArquivoBanco>) {
        lista = nova.toMutableList()
        notifyDataSetChanged()
    }
}
