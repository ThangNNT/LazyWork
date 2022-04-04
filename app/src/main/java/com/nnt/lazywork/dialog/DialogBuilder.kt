package com.nnt.lazywork.dialog

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.nnt.lazywork.databinding.ViewDialogBinding
import com.nnt.lazywork.util.convertDpToPixel

/**
 * a common dialog, custom the UI in xml layout if you want
 */
class DialogBuilder(private val context: Context) {
    private var title = ""
    private var message = ""
    private var messageSpan: Spannable? = null
    private var iconResource: Int? = null
    private var mode: DialogMode = DialogMode.CONFIRM_DIALOG
    private var isCancelable: Boolean = true

    //title
    private var titleTextColor = -1
    private var titleTextSize = -1f
    private var titleMarginTop = -1
    //private var titleFontType: FontType = FontType.SEMI_BOLD

    // message
    private var messageTextColor = -1
    private var messageTextSize = -1f
    //private var messageFontType: FontType = FontType.NORMAL
    private var messageMarginTop = -1

    //button
    private var confirmText = ""
    private var cancelText = ""
    private var confirmButtonTextColor: Int? = null
    private var cancelButtonTextColor: Int? = null
    private var confirmBackground: Int? = null
    private var cancelBackground: Int? = null
    private var buttonMarginTop = -1
    //private var buttonFontType: FontType = FontType.NORMAL
    private var buttonTextSize = -1f


    //dialog
    private var paddingTop = -1
    private var paddingBottom = -1

    //listener
    private var onConfirm: ()->Unit = {}
    private var onCancel: ()-> Unit = {}

    init {
        confirmText = "Confirm"
        cancelText = "Cancel"
    }

    fun setCancelable(isCancelable: Boolean): DialogBuilder {
        this.isCancelable = isCancelable
        return this
    }

    fun setTitle(title: String): DialogBuilder{
        this.title = title
        return this
    }

    fun setMessage(message: String): DialogBuilder{
        this.message = message
        return this
    }

    fun setMessage(messageSpannable: Spannable): DialogBuilder{
        this.messageSpan = messageSpannable
        return this
    }

    fun setIcon(@DrawableRes resource: Int): DialogBuilder{
        this.iconResource = resource
        return this
    }
    fun setDialogMode(mode: DialogMode): DialogBuilder{
        this.mode = mode
        return this
    }

    fun setTitleTextColor(@ColorInt color: Int): DialogBuilder{
        this.titleTextColor = color
        return this
    }

    fun setTitleTextSize(@Px px: Float): DialogBuilder{
        this.titleTextSize = px
        return this
    }

    fun setButtonTextSize(@Px px: Float): DialogBuilder{
        this.buttonTextSize = px
        return this
    }

    fun setMessageTextColor(@ColorInt color: Int): DialogBuilder{
        this.messageTextColor = color
        return this
    }

    fun setMessageTextSize(@Px px: Float): DialogBuilder{
        this.messageTextSize = px
        return this
    }

    fun setPaddingTop(@Px px: Int): DialogBuilder {
        this.paddingTop = px
        return this
    }

    fun setPaddingBottom(@Px px: Int): DialogBuilder{
        this.paddingBottom = px
        return this
    }

    fun setConfirmButtonText(text: String): DialogBuilder{
        this.confirmText = text
        return this
    }

    fun setCancelButtonText(text: String): DialogBuilder{
        this.cancelText = text
        return this
    }

    fun setConfirmButtonBackground(@DrawableRes res: Int): DialogBuilder{
        this.confirmBackground = res
        return this
    }

    fun setCancelButtonBackground(@DrawableRes res: Int): DialogBuilder{
        this.cancelBackground = res
        return this
    }

    fun setConfirmButtonTextColor(@ColorInt color: Int): DialogBuilder {
        this.confirmButtonTextColor = color
        return this
    }

    fun setCancelButtonTextColor(@ColorInt color: Int): DialogBuilder {
        this.cancelButtonTextColor = color
        return this
    }


    fun setOnConfirmClick(onConfirm: ()-> Unit): DialogBuilder {
        this.onConfirm = onConfirm
        return this
    }

    fun setOnCancelClick(onCancel: ()-> Unit): DialogBuilder {
        this.onCancel = onCancel
        return this
    }

    fun setTitleMarginTop(@Px px: Int): DialogBuilder {
        this.titleMarginTop = px
        return this
    }

    fun setMessageMarginTop(@Px px: Int): DialogBuilder {
        this.messageMarginTop = px
        return this
    }

    fun setButtonMarginTop(@Px px: Int): DialogBuilder {
        this.buttonMarginTop = px
        return this
    }

    fun show() {
        val layoutInflater = LayoutInflater.from(context)
        val binding = ViewDialogBinding.inflate(layoutInflater, null, false)
        val mBuilder = AlertDialog.Builder(context)
                .setView(binding.root)
        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, convertDpToPixel(10f, context).toInt())
        val mAlertDialog = mBuilder.show()
        mAlertDialog.window?.setBackgroundDrawable(inset)

        //dialog
        if(paddingTop != -1){
            val paddingLeft = binding.layoutRoot.paddingLeft
            val paddingRight = binding.layoutRoot.paddingRight
            val paddingBottom = binding.layoutRoot.paddingBottom
            val paddingTop= paddingTop
            binding.layoutRoot.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        }
        if(paddingBottom !=-1){
            val paddingLeft = binding.layoutRoot.paddingLeft
            val paddingRight = binding.layoutRoot.paddingRight
            val paddingTop = binding.layoutRoot.paddingTop
            val paddingBottom = paddingBottom
            binding.layoutRoot.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        }

        //isCancelable
        mAlertDialog.setCancelable(isCancelable)

        //title
        if(title.isEmpty()){
            binding.tvTitle.isVisible = false
            binding.tvTitle.setMarginTop(0)
        }
        else {
            binding.tvTitle.isVisible = true
            binding.tvTitle.text = title
            if(titleTextColor!=-1){
                binding.tvTitle.setTextColor(titleTextColor)
            }
            if(titleTextSize!=-1f){
                binding.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize)
            }
            if(titleMarginTop!=-1){
                binding.tvTitle.setMarginTop(titleMarginTop)
            }
        }

        //message
        if(messageSpan!=null){
            setSpannableMessage(binding.tvMessage)
        }
        else {
            setStringMessage(binding.tvMessage)
        }
        //icon
        iconResource?.let {
            binding.ivIcon.setImageResource(it)
            binding.ivIcon.isVisible = true
        }?: kotlin.run {
            binding.ivIcon.isVisible = false
            binding.tvTitle.setMarginTop(0)
        }

        //button
        confirmButtonTextColor?.let {
            binding.btnConfirm.setTextColor(it)
            binding.btnConfirmSingleMode.setTextColor(it)
        }
        cancelButtonTextColor?.let {
            binding.btnCancel.setTextColor(it)
        }

        binding.btnConfirm.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonTextSize)
        binding.btnConfirmSingleMode.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonTextSize)
        binding.btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonTextSize)

        if(mode == DialogMode.CONFIRM_DIALOG){
            binding.btnConfirm.isVisible = false
            binding.btnCancel.isVisible = false
            confirmBackground?.let {
                binding.btnConfirmSingleMode.setBackgroundResource(it)
            }
            if(confirmText.isNotEmpty()){
                binding.btnConfirmSingleMode.text = confirmText
            }
            binding.btnConfirmSingleMode.setOnClickListener {
                mAlertDialog.dismiss()
                onConfirm.invoke()
            }
        }
        else if(mode == DialogMode.YES_NO_DIALOG){
            binding.btnConfirmSingleMode.isVisible = false
            confirmBackground?.let {
                binding.btnConfirm.setBackgroundResource(it)
            }
            cancelBackground?.let {
                binding.btnCancel.setBackgroundResource(it)
            }
            if(confirmText.isNotEmpty()){
                binding.btnConfirm.text = confirmText
            }
            if(cancelText.isNotEmpty()){
                binding.btnCancel.text = cancelText
            }

            binding.btnCancel.setOnClickListener {
                mAlertDialog.dismiss()
                onCancel.invoke()
            }
            binding.btnConfirm.setOnClickListener {
                mAlertDialog.dismiss()
                onConfirm.invoke()
            }
        }
        if(buttonMarginTop!=-1){
            val marginTop= buttonMarginTop
            binding.btnConfirmSingleMode.setMarginTop(marginTop)
            binding.btnCancel.setMarginTop(marginTop)
        }
    }

    private fun View.setMarginTop(marginTop: Int){
        val layoutParams = this.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.setMargins(layoutParams.leftMargin, marginTop, layoutParams.rightMargin, layoutParams.bottomToBottom)
        this.layoutParams = layoutParams
    }

    private fun setStringMessage(textView: TextView){
        if(message.isEmpty()){
            textView.isVisible = false
            textView.setMarginTop(0)
        }
        else {
            textView.isVisible = true
            textView.text = message
            if(messageTextColor!=-1){
                textView.setTextColor(messageTextColor)
            }
            if(messageTextSize!=-1f){
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, messageTextSize)
            }
            if(messageMarginTop!=-1){
                textView.setMarginTop(messageMarginTop)
            }
        }
    }

    private fun setSpannableMessage(textView: TextView){
        if(messageSpan!=null){
            textView.isVisible = true
            textView.text = messageSpan
            if(messageTextColor!=-1){
                textView.setTextColor(messageTextColor)
            }
            if(messageTextSize!=-1f){
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, messageTextSize)
            }
            if(messageMarginTop!=-1){
                textView.setMarginTop(messageMarginTop)
            }
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    enum class DialogMode {
        CONFIRM_DIALOG,
        YES_NO_DIALOG
    }

}

