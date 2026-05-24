package com.example.fitconnect

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
    }

    override fun getItemCount() = lista.size

    fun atualizar(nova: List<ArquivoBanco>) {
        lista = nova.toMutableList()
        notifyDataSetChanged()
    }
}