package com.android.launcher3.t9.util;

import android.content.Context;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.launcher3.R;


/**
 * T9拼音键盘
 * 
 * */
public class T9View extends FrameLayout implements OnClickListener{
	private Context mContext = null;
	private TextView mT9InputEt; //显示t9盘的输入结果的text
    private T9ViewListener mT9ViewListener = null;//T9键盘输入监听器

    public T9View(Context context) {
		this(context, null);
	}

	public T9View(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public T9View(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initT9View(context);
	}

    public void setT9ViewListener(T9ViewListener mT9ViewListener) {
        this.mT9ViewListener = mT9ViewListener;
    }


    //设置显示结果的textview
    public void setTextInput(TextView mT9InputEt){
        this.mT9InputEt=mT9InputEt;

        mT9InputEt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("zhao11t9", "afterTextChanged");
                if (null != mT9ViewListener) {
                    String inputStr = s.toString();
                    mT9ViewListener.DialInputTextChanged(inputStr);
                }
            }
        });
    }
    /**
	 * 初始化T9盘
	 * 获得12个按键并绑定监听事件
	 * @param context
	 */
	private void initT9View(Context context){
		mContext = context;
		//view_9keyboard_layout.xml是12个按键的布局文件，我想把他们封装到我这个自定义的T9View.java里
		//就得通过LayoutInflater把view_9keyboard_layout.xml实例化为一个view然后addView(view)来添加到
		//T9View.java里 T9View.java是继承自FrameLayout，其他布局文件引用的时候按照如下引用就好了
		// <linlin.com.common.T9View
		// android:id="@+id/appDialerId"
		// android:layout_width="fill_parent"
		// android:layout_height="280dp">
		// </linlin.com.common.T9View>
		View view = LayoutInflater.from(context).inflate(R.layout.view_9keyboard_layout, null);
		//view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		Button key1Btn = (Button)view.findViewById(R.id.key1BtnId);
		key1Btn.setText(getAlphaFormatString("1"));
		key1Btn.setOnClickListener(this);

		Button key2Btn = (Button)view.findViewById(R.id.key2BtnId);
		key2Btn.setText(getFormatString("2 ABC"));
		key2Btn.setOnClickListener(this);

		Button key3Btn = (Button)view.findViewById(R.id.key3BtnId);
		key3Btn.setText(getFormatString("3 DEF"));
		key3Btn.setOnClickListener(this);

		Button key4Btn = (Button)view.findViewById(R.id.key4BtnId);
		key4Btn.setText(getFormatString("4 GHI"));
		key4Btn.setOnClickListener(this);

		Button key5Btn = (Button)view.findViewById(R.id.key5BtnId);
		key5Btn.setText(getFormatString("5 JKL"));
		key5Btn.setOnClickListener(this);

		Button key6Btn = (Button)view.findViewById(R.id.key6BtnId);
		key6Btn.setText(getFormatString("6 MNO"));
		key6Btn.setOnClickListener(this);

		Button key7Btn = (Button)view.findViewById(R.id.key7BtnId);
		key7Btn.setText(getFormatString("7 PQRS"));
		key7Btn.setOnClickListener(this);

		Button key8Btn = (Button)view.findViewById(R.id.key8BtnId);
		key8Btn.setText(getFormatString("8 TUV"));
		key8Btn.setOnClickListener(this);

		Button key9Btn = (Button)view.findViewById(R.id.key9BtnId);
		key9Btn.setText(getFormatString("9 WXYZ"));
		key9Btn.setOnClickListener(this);

		Button key10Btn = (Button)view.findViewById(R.id.key10Back);
		key10Btn.setOnClickListener(this);

		Button key11Btn = (Button)view.findViewById(R.id.key0BtnId);
		key11Btn.setText(getAlphaFormatString("0"));
		key11Btn.setOnClickListener(this);

		Button key12Btn = (Button)view.findViewById(R.id.key12Del);
        key12Btn.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                deleteAllDialCharacter();
                return true;
            }
        });
		key12Btn.setOnClickListener(this);

		addView(view);
	}

    public void deleteAllDialCharacter() {
        String curInputStr = mT9InputEt.getText().toString();
        if (curInputStr.length() > 0) {
            String deleteCharacter = curInputStr.substring(0,
                    curInputStr.length());
            if (null != mT9ViewListener) {
                mT9ViewListener
                        .DeleteDialCharacter(deleteCharacter);
            }
            mT9InputEt.setText("");
            Log.i("zhao11t9", "222222222");
//            ViewUtil.hideView(mDialDeleteBtn);
        }
    }
	@Override
	public void onClick(View v) {
		if(null == mT9ViewListener){
			return;
		}
        switch (v.getId()) {
            case R.id.key12Del:
                deleteSingleNumChar();
                break;
            case R.id.key0BtnId:
            case R.id.key1BtnId:
            case R.id.key2BtnId:
            case R.id.key3BtnId:
            case R.id.key4BtnId:
            case R.id.key5BtnId:
            case R.id.key6BtnId:
            case R.id.key7BtnId:
            case R.id.key8BtnId:
            case R.id.key9BtnId:
                addSingleNumChar(v.getTag().toString());
                break;

            default:
                break;
        }
    }

	//SpannableString 是貌似可以理解为超文本，可以在同一个文本里设置不同的颜色 不同的超链接之类的东东
	private SpannableString getFormatString(String srcString){
		SpannableString srcSpannableString = new SpannableString(srcString);
		//这个是改变数字的颜色
		srcSpannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.key_text_color1))
				, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		//这个是改变数字的大小的
        srcSpannableString.setSpan(new AbsoluteSizeSpan(
				(int)mContext.getResources().getDimension(R.dimen.toast_text_size))
				, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		return srcSpannableString;
	}
	
	private SpannableString getAlphaFormatString(String srcString){
		SpannableString srcSpannableString = new SpannableString(srcString);
//		srcSpannableString.setSpan(new ForegroundColorSpan(0x7fffffff), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		srcSpannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.key_text_color1))
				, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		return srcSpannableString;
	}

	private void addSingleNumChar(String addCharacter) {
		String preInputStr = mT9InputEt.getText().toString();
		if (!TextUtils.isEmpty(addCharacter)) {
			mT9InputEt.setText(preInputStr + addCharacter);
		}
	}

    public void deleteSingleNumChar() {
        String curInputStr = mT9InputEt.getText().toString();
        if (curInputStr.length() > 0) {
            String deleteCharacter = curInputStr.substring(
                    curInputStr.length() - 1, curInputStr.length());
            if (null != mT9ViewListener) {
                mT9ViewListener
                        .DeleteDialCharacter(deleteCharacter);
            }

            String newCurInputStr=curInputStr.substring(0,curInputStr.length() - 1);
            mT9InputEt.setText(newCurInputStr);
            //mT9InputEt.setSelection(newCurInputStr.length());
//            if(TextUtils.isEmpty(newCurInputStr)){
//                ViewUtil.hideView(mDialDeleteBtn);
//            }else{
//                ViewUtil.showView(mDialDeleteBtn);
//            }


        }
    }

	/**
	 * 当T9盘被操作的时候就会回调这个接口
	 */
	public interface T9ViewListener {
		/**
		 * 参数是新添加的字符
		 * @param addCharacter
		 */
		void AddDialCharacter(String addCharacter);

		/**
		 * 参数是被删除的字符
		 * @param deleteCharacter
		 */
		void DeleteDialCharacter(String deleteCharacter);

		/**
		 * 参数是整个字符串
		 * @param curCharacter
		 */
		void DialInputTextChanged(String curCharacter);

	}

}
