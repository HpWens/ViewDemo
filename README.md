# CircleMenuLayout

![](https://github.com/HpWens/ViewDemo/blob/master/app/assets/circle.gif)

![](https://github.com/HpWens/ViewDemo/blob/master/app/assets/item.gif)

## Using 

###xml

    <com.github.ws.viewdemo.widget.CircleMenuLayout
        android:id="@+id/cm"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#f0f0f0">

    </com.github.ws.viewdemo.widget.CircleMenuLayout>
    
### class

public class MainActivity extends AppCompatActivity {

    private CircleMenuLayout circleMenuLayout;

    List<Item> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        circleMenuLayout = (CircleMenuLayout) findViewById(R.id.cm);

        Item item = new Item();
        item.imageRes = R.mipmap.aql;
        item.text = "安全令";
        mList.add(item);
        item = new Item();
        item.imageRes = R.mipmap.dc;
        item.text = "订单";
        mList.add(item);
        item = new Item();
        item.imageRes = R.mipmap.cz;
        item.text = "钱袋";
        mList.add(item);
        item = new Item();
        item.imageRes = R.mipmap.dp;
        item.text = "记事本";
        mList.add(item);
        item = new Item();
        item.imageRes = R.mipmap.kb;
        item.text = "订餐";
        mList.add(item);
        item = new Item();
        item.imageRes = R.mipmap.rw;
        item.text = "快递";
        mList.add(item);


        circleMenuLayout.setAdapter(new MyAdapter());

        circleMenuLayout.setOnItemClickListener(new CircleMenuLayout.OnItemClickListener() {
            @Override
            public void onItemClickListener(View v, int position) {
                Toast.makeText(MainActivity.this, mList.get(position).text + "", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public class  MyAdapter extends  BaseAdapter{
        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
            View itemView = mInflater.inflate(R.layout.circle_menu_item, parent, false);
            initMenuItem(itemView, position);
            return itemView;
        }

        // 初始化菜单项
        private void initMenuItem(View itemView, int position) {
            // 获取数据项
            final Item item = (Item) getItem(position);
            ImageView iv = (ImageView) itemView
                    .findViewById(R.id.iv_icon);
            TextView tv = (TextView) itemView
                    .findViewById(R.id.tv_text);
            // 数据绑定
            iv.setImageResource(item.imageRes);
            tv.setText(item.text);
        }
    }
}
