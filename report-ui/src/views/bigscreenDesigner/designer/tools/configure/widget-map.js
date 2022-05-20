/*
 * @Descripttion: 中国地图 json
 * @version:
 * @Author: qianlishi
 * @Date: 2021-08-29 07:31:21
 * @LastEditors: qianlishi
 * @LastEditTime: 2021-09-28 14:17:20
 */
export const widgetMap = {
  code: 'widget-map',
  type: 'chart',
  label: '中国地图',
  icon: 'iconzhongguoditu',
  options: {
    setup: [
      {
        type: 'el-input-text',
        label: '图层名称',
        name: 'layerName',
        required: false,
        placeholder: '',
        value: '路线图',
      },
      {
        type: 'vue-color',
        label: '背景颜色',
        name: 'background',
        required: false,
        placeholder: '',
        value: ''
      },
      [
        {
          name: '标题设置',
          list: [
            {
              type: 'el-switch',
              label: '标题',
              name: 'isNoTitle',
              required: false,
              placeholder: '',
              value: true
            },
            {
              type: 'el-input-text',
              label: '标题',
              name: 'titleText',
              required: false,
              placeholder: '',
              value: ''
            },
            {
              type: 'vue-color',
              label: '字体颜色',
              name: 'textColor',
              required: false,
              placeholder: '',
              value: '#fff'
            },
            {
              type: 'el-select',
              label: '字体粗细',
              name: 'textFontWeight',
              required: false,
              placeholder: '',
              selectOptions: [
                {code: 'normal', name: '正常'},
                {code: 'bold', name: '粗体'},
                {code: 'bolder', name: '特粗体'},
                {code: 'lighter', name: '细体'}
              ],
              value: 'normal'
            },
            {
              type: 'el-input-number',
              label: '字体大小',
              name: 'textFontSize',
              required: false,
              placeholder: '',
              value: 20
            },
            {
              type: 'el-select',
              label: '字体位置',
              name: 'textAlign',
              required: false,
              placeholder: '',
              selectOptions: [
                {code: 'center', name: '居中'},
                {code: 'left', name: '左对齐'},
                {code: 'right', name: '右对齐'},
              ],
              value: 'center'
            },
            {
              type: 'el-input-text',
              label: '副标题',
              name: 'subText',
              required: false,
              placeholder: '',
              value: ''
            },
            {
              type: 'vue-color',
              label: '字体颜色',
              name: 'subTextColor',
              required: false,
              placeholder: '',
              value: ''
            },
            {
              type: 'el-select',
              label: '字体粗细',
              name: 'subTextFontWeight',
              required: false,
              placeholder: '',
              selectOptions: [
                {code: 'normal', name: '正常'},
                {code: 'bold', name: '粗体'},
                {code: 'bolder', name: '特粗体'},
                {code: 'lighter', name: '细体'}
              ],
              value: 'normal'
            },
            {
              type: 'el-input-number',
              label: '字体大小',
              name: 'subTextFontSize',
              required: false,
              placeholder: '',
              value: 12
            },
          ],
        },
        {
          name: '字体设置',
          list: [
            {
              type: 'el-input-number',
              label: '文字大小',
              name: 'fontTextSize',
              required: false,
              placeholder: '',
              value: 15,
            },
            {
              type: 'vue-color',
              label: '文字颜色',
              name: 'fontTextColor',
              required: false,
              placeholder: '',
              value: '#46bee9'
            },
            {
              type: 'el-select',
              label: '文字粗细',
              name: 'fontTextWeight',
              required: false,
              placeholder: '',
              selectOptions: [
                {code: 'normal', name: '正常'},
                {code: 'bold', name: '粗体'},
                {code: 'bolder', name: '特粗体'},
                {code: 'lighter', name: '细体'}
              ],
              value: 'normal'
            },
          ],
        },
        {
          name: '点设置',
          list: [
            {
              type: 'el-input-number',
              label: '点大小',
              name: 'pointSize',
              required: false,
              placeholder: '',
              value: 5,
            },
            {
              type: 'vue-color',
              label: '点颜色',
              name: 'pointColor',
              required: false,
              placeholder: '',
              value: '#46bee9'
            },
          ],
        },
      ]
    ],
    data: [
      {
        type: 'el-radio-group',
        label: '数据类型',
        name: 'dataType',
        require: false,
        placeholder: '',
        selectValue: true,
        selectOptions: [
          {
            code: 'staticData',
            name: '静态数据',
          },
          {
            code: 'dynamicData',
            name: '动态数据',
          },
        ],
        value: 'staticData',
      },
      {
        type: 'el-input-number',
        label: '刷新时间(毫秒)',
        name: 'refreshTime',
        relactiveDom: 'dataType',
        relactiveDomValue: 'dynamicData',
        value: 5000
      },
      {
        type: 'el-button',
        label: '静态数据',
        name: 'staticData',
        required: false,
        placeholder: '',
        relactiveDom: 'dataType',
        relactiveDomValue: 'staticData',
        value: [
          {source: '北京', target: '上海', value: 95},
          {source: '北京', target: '广州', value: 90},
          {source: '北京', target: '大连', value: 80},
          {source: '北京', target: '南宁', value: 70},
          {source: '北京', target: '南昌', value: 60},
          {source: '北京', target: '拉萨', value: 50},
          {source: '北京', target: '长春', value: 40},
          {source: '北京', target: '包头', value: 30},
          {source: '北京', target: '重庆', value: 20},
          {source: '北京', target: '常州', value: 10},
          {source: '上海', target: '包头', value: 95},
          {source: '上海', target: '昆明', value: 90},
          {source: '上海', target: '广州', value: 80},
          {source: '上海', target: '郑州', value: 70},
          {source: '上海', target: '长春', value: 60},
          {source: '上海', target: '重庆', value: 50},
          {source: '上海', target: '长沙', value: 40},
          {source: '上海', target: '北京', value: 30},
          {source: '上海', target: '丹东', value: 20},
          {source: '上海', target: '大连', value: 10},
          {source: '广州', target: '福州', value: 95},
          {source: '广州', target: '太原', value: 90},
          {source: '广州', target: '长春', value: 80},
          {source: '广州', target: '重庆', value: 70},
          {source: '广州', target: '西安', value: 60},
          {source: '广州', target: '成都', value: 50},
          {source: '广州', target: '常州', value: 40},
          {source: '广州', target: '北京', value: 30},
          {source: '广州', target: '北海', value: 20},
          {source: '广州', target: '海口', value: 10},
        ],
      },
      {
        type: 'dycustComponents',
        label: '',
        name: 'dynamicData',
        required: false,
        placeholder: '',
        relactiveDom: 'dataType',
        chartType: 'widget-mapline',
        dictKey: 'SOUTAR_PROPERTIES',
        relactiveDomValue: 'dynamicData',
        value: '',
      },
    ],
    position: [
      {
        type: 'el-input-number',
        label: '左边距',
        name: 'left',
        required: false,
        placeholder: '',
        value: 0,
      },
      {
        type: 'el-input-number',
        label: '上边距',
        name: 'top',
        required: false,
        placeholder: '',
        value: 0,
      },
      {
        type: 'el-input-number',
        label: '宽度',
        name: 'width',
        required: false,
        placeholder: '该容器在1920px大屏中的宽度',
        value: 600,
      },
      {
        type: 'el-input-number',
        label: '高度',
        name: 'height',
        required: false,
        placeholder: '该容器在1080px大屏中的高度',
        value: 400,
      },
    ]
  }
}