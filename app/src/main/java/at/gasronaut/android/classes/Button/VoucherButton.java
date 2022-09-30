package at.gasronaut.android.classes.Button;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Button;

import at.gasronaut.android.classes.Voucher;

@SuppressLint("AppCompatCustomView")
public class VoucherButton extends Button {
    public boolean clicked = false;
    private Voucher voucher = null;

    public VoucherButton(Context context) {
        super(context);

        /*
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = !clicked;
            }
        });
        */
    }

    public void setVoucher(Voucher v) {
        this.voucher = v;
    }

    public Voucher getVoucher() {
        return this.voucher;
    }
}
