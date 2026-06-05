package com.example.fitconnect.feature.feedback

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class FeedbackHistoricoAdapter(private var lista: List<FeedbackBanco>) :
    RecyclerView.Adapter<FeedbackHistoricoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNome: TextView = view.findViewById(R.id.tv_nome_treino)
        val tvBadge: TextView = view.findViewById(R.id.tv_badge_intensidade)
        val tvData: TextView = view.findViewById(R.id.tv_data_feedback)
        val tvComentario: TextView = view.findViewById(R.id.tv_comentario_feedback)
        val tvRespostaPersonal: TextView = view.findViewById(R.id.tv_resposta_personal_feedback)
        val layoutRespostaPersonal: View = view.findViewById(R.id.layout_resposta_personal_feedback)
        val ivIcone: ImageView = view.findViewById(R.id.iv_icone_feedback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback_historico, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fb = lista[position]

        holder.tvNome.text = fb.treino_nome

        when (fb.intensidade) {
            "Intenso" -> {
                holder.tvBadge.text = "INTENSO"
                holder.tvBadge.setTextColor(0xFFE53935.toInt())
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_intenso)
                holder.ivIcone.setImageResource(R.drawable.ic_intensidade_intenso)
                holder.ivIcone.setColorFilter(0xFFE53935.toInt())
            }
            "Moderado" -> {
                holder.tvBadge.text = "MODERADO"
                holder.tvBadge.setTextColor(0xFFFF9800.toInt())
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_moderado)
                holder.ivIcone.setImageResource(R.drawable.ic_intensidade_moderado)
                holder.ivIcone.setColorFilter(0xFFFF9800.toInt())
            }
            else -> {
                holder.tvBadge.text = "FÁCIL"
                holder.tvBadge.setTextColor(0xFF4CAF50.toInt())
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_facil)
                holder.ivIcone.setImageResource(R.drawable.ic_intensidade_leve)
                holder.ivIcone.setColorFilter(0xFF4CAF50.toInt())
            }
        }

        holder.tvData.text = formatarData(fb.criado_em)

        if (fb.observacoes.isBlank()) {
            holder.tvComentario.text = "Sem comentários registrados"
            holder.tvComentario.setTextColor(0xFF555555.toInt())
            holder.tvComentario.setTypeface(null, android.graphics.Typeface.ITALIC)
        } else {
            holder.tvComentario.text = "\"${fb.observacoes}\""
            holder.tvComentario.setTextColor(0xFFA0A0A0.toInt())
            holder.tvComentario.setTypeface(null, android.graphics.Typeface.ITALIC)
        }

        val resposta = fb.resposta_personal.orEmpty().trim()
        if (resposta.isEmpty()) {
            holder.layoutRespostaPersonal.visibility = View.GONE
        } else {
            holder.layoutRespostaPersonal.visibility = View.VISIBLE
            holder.tvRespostaPersonal.text = resposta
        }
    }

    override fun getItemCount() = lista.size

    fun atualizar(novaLista: List<FeedbackBanco>) {
        lista = novaLista
        notifyDataSetChanged()
    }

    private fun formatarData(raw: String): String {
        return try {
            val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val saida = SimpleDateFormat("dd 'de' MMMM, HH:mm", Locale("pt", "BR"))
            val date = entrada.parse(raw.take(19)) ?: return raw
            saida.format(date)
        } catch (e: Exception) {
            raw
        }
    }
}
