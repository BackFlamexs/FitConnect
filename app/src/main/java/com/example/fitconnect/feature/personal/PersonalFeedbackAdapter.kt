package com.example.fitconnect.feature.personal

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class PersonalFeedbackAdapter(
    private var lista: List<FeedbackBanco>,
    private val onResponder: (FeedbackBanco) -> Unit
) : RecyclerView.Adapter<PersonalFeedbackAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTreino: TextView = view.findViewById(R.id.tv_personal_feedback_treino)
        val tvInfo: TextView = view.findViewById(R.id.tv_personal_feedback_info)
        val tvObs: TextView = view.findViewById(R.id.tv_personal_feedback_obs)
        val tvBadge: TextView = view.findViewById(R.id.tv_personal_feedback_badge)
        val tvResposta: TextView = view.findViewById(R.id.tv_personal_feedback_resposta)
        val layoutResposta: View = view.findViewById(R.id.layout_personal_feedback_resposta)
        val ivIcone: ImageView = view.findViewById(R.id.iv_personal_feedback_icone)
        val btnResponder: Button = view.findViewById(R.id.btn_responder_feedback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback_personal, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val feedback = lista[position]
        holder.tvTreino.text = feedback.treino_nome
        holder.tvInfo.text = "${feedback.duracao_min} min • ${formatarData(feedback.criado_em)}"
        configurarIntensidade(holder, feedback.intensidade)

        if (feedback.observacoes.isBlank()) {
            holder.tvObs.text = "Sem comentarios registrados"
            holder.tvObs.setTextColor(0xFF555555.toInt())
            holder.tvObs.setTypeface(null, android.graphics.Typeface.ITALIC)
        } else {
            holder.tvObs.text = "\"${feedback.observacoes}\""
            holder.tvObs.setTextColor(0xFFA0A0A0.toInt())
            holder.tvObs.setTypeface(null, android.graphics.Typeface.ITALIC)
        }

        val resposta = feedback.resposta_personal.orEmpty().trim()
        holder.layoutResposta.visibility = if (resposta.isEmpty()) View.GONE else View.VISIBLE
        holder.tvResposta.text = resposta
        holder.btnResponder.text = if (resposta.isEmpty()) "RESPONDER" else "EDITAR RESPOSTA"
        holder.btnResponder.setOnClickListener { onResponder(feedback) }
    }

    override fun getItemCount(): Int = lista.size

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
            raw.take(10)
        }
    }

    private fun configurarIntensidade(holder: VH, intensidade: String) {
        when (intensidade) {
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
                holder.tvBadge.text = "FACIL"
                holder.tvBadge.setTextColor(0xFF4CAF50.toInt())
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_facil)
                holder.ivIcone.setImageResource(R.drawable.ic_intensidade_leve)
                holder.ivIcone.setColorFilter(0xFF4CAF50.toInt())
            }
        }
    }
}
