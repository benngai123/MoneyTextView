package ben.moneytextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class MoneyTextView extends AppCompatTextView {

	private String mCurrency;
	private boolean mIsShowCurrency;
	private boolean mIsSimplyAmount;
	private boolean mIsCurrencyAtEnd;
	private boolean mIsAbsAmount;

	public MoneyTextView(Context context) {
		super(context);
		initView(context, null);
	}

	public MoneyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context, attrs);
	}

	private void initView(Context context, AttributeSet attrs) {
		// Setting Default Parameters
		mCurrency = "";
		mIsShowCurrency = false;//默认不展现货币
		mIsSimplyAmount = true;//默认(VND,KHR 不保留小数)
		mIsCurrencyAtEnd = true;//默认货币符号展现在货币之后
		mIsAbsAmount = false;//默认不取绝对值

		// Check for the attributes
		if (attrs != null) {
			// Attribute initialization
			final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.MoneyTextView, 0, 0);
			try {
				mCurrency = attrArray.getString(R.styleable.MoneyTextView_currency);
				mIsShowCurrency = attrArray.getBoolean(R.styleable.MoneyTextView_show_currency, false);
				mIsSimplyAmount = attrArray.getBoolean(R.styleable.MoneyTextView_simply_amount, true);
				mIsCurrencyAtEnd = attrArray.getBoolean(R.styleable.MoneyTextView_currency_at_end, true);
				mIsAbsAmount = attrArray.getBoolean(R.styleable.MoneyTextView_abs_amount, false);
			} finally {
				attrArray.recycle();
			}
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setValue(getText().toString());
	}

	private String formatValue(String number) {
		String decoStr = null;
		try {
			if (mIsAbsAmount) {
				number = absAmount(number);
			}

			if (mIsSimplyAmount) {
				if (TextUtils.isEmpty(mCurrency)) return format(number, 2);
				decoStr = format(number, mCurrency.equals("KHR") || mCurrency.equals("VND") ? 0 : 2);
			} else {
				decoStr = format(number, 2);
			}

			if (mIsShowCurrency && !TextUtils.isEmpty(mCurrency)) {
				if (mIsCurrencyAtEnd) {
					decoStr = decoStr + " " + mCurrency;
				} else {
					decoStr = mCurrency + " " + decoStr;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return decoStr;
	}

	private void setValue(String valueStr) {
		try {
			String originalString;

			String stringVal;

			originalString = getValueString();
			stringVal = format(originalString, 2);
			String formattedString = formatValue(stringVal);

			//setting text after format to EditText
			setText(formattedString);

		} catch (NumberFormatException e) {
			e.printStackTrace();
			setText(valueStr);
		}
	}

	/**
	 * Get the value of the text without any commas and currency.
	 * For example, if the edit text value is USD 1,34,000.60 then this method will return 134000.60
	 *
	 * @return A string of the raw value in the text field
	 */
	public String getValueString() {

		String string = getText().toString();

		try {
			if (string.contains(",")) {
				string = string.replace(",", "");
			}
			if (string.contains(" ") && !mIsCurrencyAtEnd) {
				string = string.substring(string.indexOf(" ") + 1, string.length());
			} else if (string.contains(" ") && mIsCurrencyAtEnd) {
				string = string.substring(0, string.indexOf(" "));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return string;
	}

	/**
	 * Get the value of the text with formatted commas and currency.
	 * For example, if the edit text value is USD 1,34,000.60 then this method will return exactly USD 1,34,000.60
	 *
	 * @return A string of the text value in the text field
	 */
	public String getFormattedString() {
		return getText().toString();
	}

	/**
	 * Set the currency for the edit text. (Default is USD).
	 *
	 * @param currency the new currency in string
	 */
	public void setCurrency(String currency) {
		mCurrency = currency;
		setValue(getText().toString());
	}

	/**
	 * Shows the currency in the text. (Default is not shown).
	 */
	public void showCurrency() {
		mIsShowCurrency = true;
		setValue(getText().toString());
	}

	/**
	 * 设置 Currency 位置
	 * true  : amount之后(默认)
	 * false : amount之前
	 */
	public void setCurrencyAtEnd(boolean isCurrencyAtEnd) {
		mIsCurrencyAtEnd = isCurrencyAtEnd;
		setValue(getText().toString());
	}

	/**
	 * Simplify the money in the text. (Default is simplified).
	 * "KHR" & "VND" have no decimal
	 */
	public void setSimplyMoney(boolean isSimplyAmount) {
		mIsSimplyAmount = isSimplyAmount;
		setValue(getText().toString());
	}

	public void setMoney(String currency, String amount) {
		setText(amount);
		setCurrency(currency);
	}

	/**
	 * Abs the money in the text. (Default is not abs).
	 */
	public void setAbsAmount(boolean isAbsAmount) {
		mIsAbsAmount = isAbsAmount;
		setValue(getText().toString());
	}

	private String format(CharSequence value, int digits) {
		try {
			if (TextUtils.isEmpty(value) || value.equals(".")) return "";
			String s = value.toString().replace(",", "");
			if (TextUtils.isEmpty(s)) return "";
			if (digits < 0) digits = 0;
			if (digits > 10) digits = 10;
			BigDecimal b = new BigDecimal(s);
			double d = b.setScale(digits, BigDecimal.ROUND_HALF_UP).doubleValue();
			NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
			nf.setMaximumFractionDigits(digits);
			nf.setMinimumFractionDigits(digits);
			return nf.format(d);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private Double strToDouble(String value) {
		try {
			if (value == null || value.equals("") || value.equals("null")) return 0.00;
			value = value.replace(",", "");
			return Double.parseDouble(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.00;
	}

	private String absAmount(String amount) {
		Double amount2 = strToDouble(amount);
		return String.valueOf(Math.abs(amount2));
	}

}
