/**
 * Created by wenben on 2017/12/1.
 */
require(['jquery','knockout', 'bootstrap', 'uui','tree', 'grid', 'ip','csof'], function($,ko) {
    window.ko = ko;

    var options = ip.getCommonOptions({}),
        tokenid = ip.getTokenId();//通过ip.js获取token值


    /*
    "/df/supervise/newExcel/excel.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
     "&menuname=" + options.svMenuName+"&entityId="+execlData.entity_id+"&entityName=csof_sup_bill&oid="+execlData.oid+
     "&dep_id="+execlData.dep_id+"&dep_code="+execlData.dep_code+"&modelFlag=0&admin="+execlData.edit+"&modular="+execlData.modular+
     "&saveBtn="+execlData.saveBtn+"&billtypeCode="+execlData.billtypeCode+"&billId="+execlData.billId+"&objtypeId="+execlData.objtypeId+
     "&objId="+execlData.objId+"&supCycle="+execlData.supCycle+"&supDate="+execlData.supDate;
     */
    /* 获取请求url */
    var url = window.location.href;

    var entityName = url.split("entityName=")[1] ? url.split("entityName=")[1].split("&")[0] : 'weared',
        oid = url.split("oid=")[1] ? url.split("oid=")[1].split("&")[0] : '',
        entity_id = url.split("entity_id=")[1] ? url.split("entity_id=")[1].split("&")[0] : '',
        dep_id = url.split("dep_id=")[1] ? url.split("dep_id=")[1].split("&")[0] : '',
        dep_code = url.split("dep_code=")[1] ? url.split("dep_code=")[1].split("&")[0] : '',
        modelFlag = url.split("modelFlag=")[1] ? url.split("modelFlag=")[1].split("&")[0] : '',
        modular = url.split("modular=")[1] ? url.split("modular=")[1].split("&")[0] : 'excel',
        saveBtn = url.split("saveBtn=")[1] ? url.split("saveBtn=")[1].split("&")[0] : '',
        billtypeCode = url.split("billtypeCode=")[1] ? url.split("billtypeCode=")[1].split("&")[0] : '',
        billId = url.split("billId=")[1] ? url.split("billId=")[1].split("&")[0] : '',
        objtypeId = url.split("objtypeId=")[1] ? url.split("objtypeId=")[1].split("&")[0] : '',
        supCycle = url.split("supCycle=")[1] ? url.split("supCycle=")[1].split("&")[0] : '',
        supDate = url.split("supDate=")[1] ? url.split("supDate=")[1].split("&")[0] : '',
        objId = url.split("objId=")[1] ? url.split("objId=")[1].split("&")[0] : '',
        admin= url.split("admin=")[1] ? url.split("admin=")[1].split("&")[0] : 'Y';
        isImp = url.split("isImp=")[1] ? url.split("isImp=")[1].split("&")[0] : 'Y',
        useviewId = url.split("viewId=")[1] ? url.split("viewId=")[1].split("&")[0] : '';
    var pViewId = '';//记录是否时已存在的模版,若有pViewId不为空，若无，则为空


    //根据参数saveBtn判断是否需要保存按钮，若为N，隐藏；否则，显示
    if(saveBtn == 'N') {
        $('#saveExc').hide();
    } else {
        $('#saveExc').show();
    }
    
    if(isImp == 'Y'){
    	$("#loadIn").show();
    	$("#loadDataIn").show();
    	$("#loadOut").show();
    }else{
    	$("#loadIn").hide();
    	$("#loadDataIn").hide();
    	$("#loadOut").hide();
    }
    
    if(admin == "Y"){
    	$(".btn-list").show();
    }else{
    	$(".btn-list").hide();
    }


    var requireUrl = {
        SAVE_VIEW : '/CsofCView/saveCsofView.do?tokenid='+ tokenid + '&ajax=noCache',  //保存选中文件生成viewId
        SAVE_EXCEL : '/CsofCView/saveExcel.do?tokenid='+ tokenid + '&ajax=noCache',  //保存新增、修改数据
        GET_EXCEL : '/CsofCView/getExcel.do?tokenid='+ tokenid + '&ajax=noCache',  //通过viewId获取列表视图数据
        SAVE_EXCEL_DATA: '/CsofCView/saveExcelData.do?tokenid='+ tokenid + '&ajax=noCache',//通过导入excel导入数据
        SELECT_ALL_EXCEL:'/CsofCView/selectAllExcel.do?tokenid='+ tokenid + '&ajax=noCache',//获取保存的所有view中excel表名及ID
        GET_DATA : '/CsofCView/getData.do?tokenid='+ tokenid + '&ajax=noCache',//获取excel中数据
    };

    var excelAllData = [],//全局变量定义excel显示的所有数据
        excelAllView = {};//全局变量定义excel显示的所有view结构

    var isEdit = false;
    var excelData = [];//表头的非空数据
    var bodyData = {};//根据SHEET_ID

    /* 实现标签页数据分离 */
    var pageObj = {};//存储页签的id name
    var pageArr = [];//存储页签的长度和id值




    /* 合并body footer数据 */
    /* excelData =  excelData.concat(excelAllView.body);
     excelData =  excelData.concat(excelAllView.foot);*/

    var viewModel = {};

    /* 公共函数类  class commonFn */
    var commonFn = {
        /* 实现金额的千分符显示 */
        toThousands : function (num) {
        	if(num == '-') {
        		num = '0' ;
        	}
        	if(typeof num != 'String') {
        		num = '' + num;
        	}
            var flag = false;//一个标记判断是否存在小数输入
            var lastNum = '00' ;//保存小数后面的数值
            if(num.split('.')[1]){//若存在输入小数点，则处理小数点左边的数字
                lastNum = num.split('.')[1].length >= 2 ? num.split('.')[1].substring(0,2) : num.split('.')[1] + '0';
                num = num.split('.')[0];
                flag = true;
            }
            var result = [ ], counter = 0;
            num = (num || 0).toString().split('');
            for (var i = num.length - 1; i >= 0; i--) {
                counter++;
                result.unshift(num[i]);
                if (!(counter % 3) && i != 0) { result.unshift(','); }
            }
            result = result.join('');

            result = result + '.' +lastNum;

            return result;
        },

        /* 获取对象的长度 */
        getObjLength: function (obj) {
            var count = 0;
            if(typeof obj == 'object'){
                $.each(obj, function (key, val) {
                    count++;
                });
            };
            return count;
        },

        /* 实现数字的百分比显示 */
        toPrecent: function (num) {
            if(Number(num) != NaN){//将字符串数字转化为数字，判断是不是数字
                num = parseFloat(Number(num) * 100);
                if(num.toString().split('.') == num.toString()){//判断是否存在小数点，若不存在
                    return num + '.0%' ;
                }else{
                    return num.toFixed(2) + '%';
                }

            }else{
                ip.ipInfoJump('您的输入格式错误！','info');
                return;
            }

        },

        /*
         * name ： 数据中的edit_type
         * 这个函数类似与switch（name）的写法
         * */
        findType : function (name) {//查找文本编辑类型
            var names = {
                '#S': function () {//短文本（最长64位，30个汉字） 左对齐
                    return {
                        type : 'text',
                        textAlign : 'left'
                    };
                },
                '#T': function () {//长文本（最长300位，150个汉字） 左对齐
                    return {
                        type : 'text',
                        textAlign : 'left'
                    };
                },
                '#N': function () {//数字(12)  居中
                    return {
                        type : 'number',
                        textAlign : 'center'
                    };
                },
                '#D': function () {//	日期（yyyy-mm-dd）  居中
                    return {
                        type : 'date',
                        textAlign : 'center'
                    };
                },
                '#M': function () {//金额(1230.2  右对齐
                    return {
                        type : 'money',
                        textAlign : 'right'
                    };
                },
                '#P': function () {//百分比(0.2=>20.0%)  右对齐
                    return {
                        type : 'precent',
                        textAlign : 'right'
                    };
                },
                '-': function () {//空值 ‘-’
                    return {
                        type : 'text',
                        textAlign : 'center'
                    };
                }
            };
            if(typeof names[name] != 'function') {
                return false;
            }
            return names[name]();//返回一个函数
        },

        /*
         * tableArr ： 构建table的数据
         * tabId ： 页签的id
         * positive: 数据存放位置
         * */
        buildTable: function (tableArr, tabId, positive) {
            var html,//需要添加的html
                arr = [],//备用数组
                arr2 = [];//二维数组

            /*
             * 思路 ： 1.先将所有数据进行分组，组成一个二维数组，[[],[],[].....]
             * 2.通过CELL_CODE对数据进行排序
             * 3.将数组遍历，形成表格
             * */
            var rowIndex = {};//记录遍历过的row值
            //构建基础模版
            $.each(tableArr, function (index, item) {
                var col = item.COL_INDEX;//此时取得值为字符串
                var row = item.ROW_INDEX - 1 + '';//对字符串进行运算强转为number，所以后面加上‘’转化为字符串形式， 赋值为key值
                //将字符串数字装换成数字
                //item.CELL_TXT += col+ '' + row;
                if(rowIndex[row] != undefined){//判断数组是否存在，若不存在，设置二维数组下的数为数组Arr
                    arr2[rowIndex[row]].push(item);
                }else{
                    rowIndex[row] = commonFn.getObjLength(rowIndex);//由于对象起始长度为0，数组起始index从0开始，即arr2[0] = []
                    arr2[rowIndex[row]] = [];
                    arr2[rowIndex[row]].push(item);
                };
            });


            var newArr2 = [];

            $.each(arr2, function (index, item) {
                if(item == undefined) {
                    return;
                }else{
                    newArr2.push(item);
                }
            });

            arr2 = newArr2;
            $.each(arr2, function (index, item) {
                html += '<tr class="tr'+ index +'" data-index="'+ (index + 1) +'"></tr>';
            });
            //先将所有的tr设定好
            //$('tbody').html(html);
            $('#' + tabId).find(positive).html(html);

            /* 构建显示数据td */
            $.each(arr2, function (index1,item1) {
            	/*$.each(excelAllView.head, function (index, item) {
                    if(item.COLSPAN == '1' && item.ROWSPAN == '1' && item.IS_SPAN == '1'){
                        return;
                    }else{
                        excelData.push(item);
                    }
                });*/
                var tdHtml = '';
                $.each(item1, function (index2, item2) {
                	if(item2.COLSPAN == '1' && item2.ROWSPAN == '1' && item2.IS_SPAN == '1') {
                		//;
                	} else {
                		if(item2 == '' ||item2 == undefined){
                            tdHtml += '<td></td>';
                            //tdHtml += '<td style="height: 50px;width: 50px;"></td>'
                        }else{
                            //tdHtml += '<td data-id="'+ item2.CSOF_CODE +'" COLSPAN="'+ item2.COLSPAN +'" ROWSPAN="'+ item2.ROWSPAN +'" data-type="'+ item2.INPUT_TYPE +'" data-index="'+ item2.ROW_INDEX +'" data-edit="'+ item2.IS_EDIT +'" style="height:38px;width:'+ item2.CELL_WIDTH +';text-align:'+  commonFn.findType(item2.INPUT_TYPE).textAlign+' ;">'+ item2.CELL_TXT +'</td>';
                            //tdHtml += '<td COLSPAN="'+ item2.COLSPAN +'" ROWSPAN="'+ item2.ROWSPAN +'" style="height:38px;width:'+ item2.CELL_WIDTH +';'+ item2.cell_style +'">'+ item2.CELL_TXT +'</td>';
                            if(item2.INPUT_TYPE == '#M'){
                                tdHtml += '<td data-id="'+ item2.CSOF_CODE +'" COLSPAN="'+ item2.COLSPAN +'" ROWSPAN="'+ item2.ROWSPAN +'" data-type="'+ item2.INPUT_TYPE +'" data-index="'+ item2.ROW_INDEX +'" data-edit="'+ item2.IS_EDIT +'" style="height:38px;text-align:'+  commonFn.findType(item2.INPUT_TYPE).textAlign+' ;">'+ commonFn.toThousands(item2.CELL_TXT) +'</td>';
                            }else if(item2.INPUT_TYPE == '#P'){
                                tdHtml += '<td data-id="'+ item2.CSOF_CODE +'" COLSPAN="'+ item2.COLSPAN +'" ROWSPAN="'+ item2.ROWSPAN +'" data-type="'+ item2.INPUT_TYPE +'" data-index="'+ item2.ROW_INDEX +'" data-edit="'+ item2.IS_EDIT +'" style="height:38px;text-align:'+  commonFn.findType(item2.INPUT_TYPE).textAlign+' ;">'+ commonFn.toPrecent(item2.CELL_TXT) +'</td>';
                            }else{
                                tdHtml += '<td data-id="'+ item2.CSOF_CODE +'" COLSPAN="'+ item2.COLSPAN +'" ROWSPAN="'+ item2.ROWSPAN +'" data-type="'+ item2.INPUT_TYPE +'" data-index="'+ item2.ROW_INDEX +'" data-edit="'+ item2.IS_EDIT +'" style="height:38px;text-align:'+  commonFn.findType(item2.INPUT_TYPE).textAlign+' ;">'+ item2.CELL_TXT +'</td>';
                            }

                        }
                	}
                    
                });
                //$('.tr'+ index1).html(tdHtml);
                $('#' + tabId + ' '+positive).find('.tr'+ index1).html(tdHtml);

            });
        },

        /* 获取所有数据 */
        getExcData: function () {
            /* 表头数据获取 */
            var data = {
                data: '',
                view: {
                    head : '',
                    body : '',
                    foot : ''
                }
            };
            data.view.head = excelData;
            $.each(data.view.head, function (index, item) {//根据每个td 的data-id属性获取每个单元格的值，然后对原始数据值进行替换
                item.CELL_TXT = $('#'+item.SHEET_ID +' thead td[data-id="' + item.CSOF_CODE + '"]').text();
            });

            /* 表尾结构获取 */
            data.data = excelAllData;
            data.view.foot = excelAllView.foot;
            $.each(data.view.foot, function (index, item) {//根据每个td 的data-id属性获取每个单元格的值，然后对原始数据值进行替换
                item.CELL_TXT = $('#'+item.SHEET_ID +' tfoot td[data-id="' + item.CSOF_CODE + '"]').text();
            });

            /* 获取表内容的数据
             * 根据索引index获取每行值的内容，将每行值的CSOF_CODE填入值
             * */
            var getBodyArr = [];//最终将获取的body数据data
            data.view.body = excelAllView.body;
            //循环每个页签的内容
            //获取每个页签的data，取出对应的index_row
            //利用view.body的模版和ROW_INDEX获取相对应的值，填入CSOF_CODE内容里
            //实现data数据保存，view.body模版数据不变，将数据怎么来，怎么传递给后台
            $.each(pageArr, function (index1, item1) {
                var pageListArr = [];
                $.each(data.data, function (index2, item2) {
                    if(item2.SHEET_ID == item1) {//页签id进行判断,对data进行分类
                        pageListArr.push(item2);
                    }
                });
                var pageViewBodyArr = [];
                $.each(data.view.body, function (index2, item2) {
                    if(item2.SHEET_ID == item1) {//页签id进行判断,对data进行分类
                    	pageViewBodyArr.push(item2);
                    }
                });
                pageListArr.sort(commonFn.sortByPro('ROW_INDEX'));//将数据按照rowIndex进行排序
                $.each(pageListArr, function (index3, item3) {
                    $.each(pageViewBodyArr, function (index, item) {//根据每个td 的data-id属性获取每个单元格的值，然后对原始数据值进行替换
                    	item3.ROW_INDEX = index3 + 1 + '';
                    	item3[item.CSOF_CODE] = $('#'+ item1 +' tbody tr[data-index="'+ item3.ROW_INDEX +'"] td[data-id="' + item.CSOF_CODE + '"]').text();
                        if(item.INPUT_TYPE == '#M') {
                        	item3[item.CSOF_CODE] = $('#'+ item1 +' tbody tr[data-index="'+ item3.ROW_INDEX +'"] td[data-id="' + item.CSOF_CODE + '"]').text().split(',').join('');
                        }
                    });
                    getBodyArr.push(item3);
                });
            });

            data.data = getBodyArr;
            return data;
        },

        /* 构建表的body部分
         * tabArr: 页签的数组
         * viewBody: view.body模版数组
         * detailData: 数据详情 data
         * */
        buildBody: function (tabArr, viewBody, detailData) {
            $.each(tabArr, function (index1, item1) {
                var dataArr = [];//根据SHEET_ID分出的数据数组
                var conArr = [];//获取表的data数据
                var lastData = [];//最终要生成内容的数据
                $.each(viewBody, function (index2, item2) {
                    if(item2.SHEET_ID == item1) {
                        dataArr.push(item2);
                    }
                });

                if(detailData.length > 0) {
                    //提取data数据
                    $.each(detailData, function (index2, item2) {
                        if(item2.SHEET_ID == item1) {
                            conArr.push(item2);
                        }
                    });
                }

                //如果view.body中含有合计字段，而数据中的数据存储并没有合计C_HJ，那么将数据中加入一个空的合计
                $.each(dataArr, function (index, item) {//先遍历模版view.body数组，将数据中未存在的csof_code补充完
                    var num = 0;
                    $.each(conArr, function (index2, item2) {//遍历数据中的所有字段，查询是否有view.body中的字段，若有num加一
                        $.each(item2, function (key, val) {
                            if(item.CSOF_CODE == key) {
                                num++;
                            }
                        });
                        if(num == 0) {
                            item2[item.CSOF_CODE] = '';
                        }
                    });
                });

                //将data数据塞进view.body里生成多行数据
                $.each(conArr, function (index, item) {
                    var copyData = [];//每个表内容模版的复制体
                    var indexRow = item.ROW_INDEX;//提取每条数据的index
                    $.each(item, function (key, val) {
                        $.each(dataArr, function (index3, item3) {
                            if(item3.CSOF_CODE == key){
                                //item3.ROW_INDEX += indexRow;
                                item3.ROW_INDEX = Number(indexRow) + Number(item3.ROW_INDEX);//将每条数据的ROW_INDEX加上data数据的index形成不同的数据行
                                item3.CELL_TXT = val;
                            }
                        });

                    });
                    //copyData = dataArr;
                    //由于上述直接赋值只会将地址赋值过去，不会改变数组或者对象的target，导致最终所有的数组都一样
                    //注： 深度复制会导致内容过度消耗
                    //数据深度复制，$.extend(bool, obj1, obj2)
                    //bool : 是否进行深度复制
                    copyData = $.extend(true, [], dataArr );
                    lastData = lastData.concat(copyData);
                });
                commonFn.buildTable(lastData, item1, 'tbody');//实现构建表内容
                if(!detailData.length) {
                    $('tbody').html('');
                }
            });
        },

        //获取当前页签的data数据结构,bodyView : view.body,页签id: SHEET_ID,当data无数据时，调用此函数
        getBaseData: function (id, bodyView) {
            var baseData = {};
            baseData.ROW_INDEX = '1';
            $.each(bodyView, function (index, item) {
                if(item.SHEET_ID == id) {
                    baseData.SHEET_ID = id;
                    baseData.VIEW_ID = item.VIEW_ID;
                    baseData.id = item.id;
                    baseData[item.CSOF_CODE] = '';
                }
            });
            if(baseData.SHEET_ID) {
            	//
            } else {
            	ip.ipInfoJump('不是表单列表，不支持新增行！', 'info');
            }
            
            return baseData;
        },
        
        //判断一个objec是否时空对象
        isEmptyObj: function (obj) {
            for (var key in obj){
                return false;//返回false，不为空对象
            }
            return true;//返回true，为空对象
        },
        
        //根据某个属性值进行排序,通过sort方法调用  arr.sort(sortByPro(pro))
        sortByPro: function (pro) {
            return function(a,b){
                var value1 = a[pro];
                var value2 = b[pro];
                return value1 - value2;
            };
        }
    };


    /* 主功能，实现excel页面显示 */
    function showExcel(test) {
        excelData = [];
        
        
        excelData = excelAllView.head;

        pageObj = {};
        /* 将head的数据的为空的数据去除掉 */
        $.each(excelAllView.head, function (index, item) {
            if(pageObj[item.SHEET_ID] == undefined){
                pageObj[item.SHEET_ID] = item.SHEET_NAME;
                pageArr.push(item.SHEET_ID);
            }
        });

        var tabHtml = '';//标签头html
        var tabConHtml = '';
        $.each(pageObj, function (key, val) {
            tabHtml += '<li><a href="#'+ key +'" data-toggle="tab">'+ val +'</a></li>';//便签头html代码生成
            tabConHtml += '<div class="tab-pane fade" id="'+ key +'"><table class="table table-bordered"><thead></thead><tbody></tbody><tfoot></tfoot></table></div>';
        });

        /* 实现页签 */
        $('#myTab').html(tabHtml);
        $('#myTab').find('li').eq(0).addClass('active');
        $('#myTabContent').html(tabConHtml);
        $('#myTabContent').find('.tab-pane').eq(0).addClass('in active ');
        
        //将所有的数据按照sheetId进行分类
        var excelAllDataObj = {};
        $.each(excelAllData, function(index, item) {
        	if (excelAllDataObj[item.SHEET_ID]) {
        		excelAllDataObj[item.SHEET_ID].push(item);
        	} else {
        		excelAllDataObj[item.SHEET_ID] = [];
        		excelAllDataObj[item.SHEET_ID].push(item);
        	}
        });
        
      /*  //将所有数据的rowIndex变为从1开始
        var copyExcelAllDataObj = excelAllDataObj;
        var copyExcelAllData = [];
        $.each(copyExcelAllDataObj, function(key, val) {
        	val.sort(commonFn.sortByPro(ROW_INDEX));
        	$.each(val, function(index, item) {
        		//将排序好的值由小到大排序，取第一个的rowIndex值，将里面所有的rowIndex前去第一个所需要减去的值变为1，得出needReduceVal，遍历减值
        		var needReduceVal = val[0] - 1;
        		item.ROW_INDEX = item.ROW_INDEX - needReduceVal;
        	});
        	copyExcelAllData.push(val);
        });
        
        excelAllData = copyExcelAllData;*/
        

        /* 将数据中属于head部分填入想对应的区域
         * 由于对于foot和head而言，excel数据中的每一条数据都会有is_edit
         * 为1的字段值，所以若数据存在，则取数据中的第一条
          * */
        
        if(excelAllData.length > 0) {
        	
        	//由于无论是data数据还是view数据，都存在很多不同的页签表数据，所以需要唉取第一条数据之前对数据进行分类，才能取第一个数据
        	$.each(excelData, function(index, item) {
        		if(excelAllDataObj[item.SHEET_ID]) {
        			$.each(excelAllDataObj[item.SHEET_ID][0], function(key, val) {
        				if(item.CSOF_CODE == key) {
                            item.CELL_TXT = val;
                        }
        			});
        		}
        	});
        	
            /*$.each(excelAllData[0], function (key, val) {
                $.each(excelData, function (index, item) {//若是数据中的key与view.head的csof_code匹配，那么将值填入
                    if(item.CSOF_CODE == key) {
                        item.CELL_TXT = val;
                    }
                });
            });*/
        }


        /* 构建表头 */
        $.each(pageArr, function (index1, item1) {
            var dataArr = [];//根据SHEET_ID分出的数据数组
            $.each(excelData, function (index2, item2) {
                if(item2.SHEET_ID == item1) {
                    dataArr.push(item2);
                }
            });
            commonFn.buildTable(dataArr, item1, 'thead');//实现构建表
        });

        /* 构建表内容 */
        commonFn.buildBody(pageArr, excelAllView.body, excelAllData);

        /* 去除表尾的空字符单元格 */
        var footViewData = [];
        $.each(excelAllView.foot, function (index, item) {
            if(item.CELL_TXT == '-' && item.IS_SPAN == '1'){
                return;
            }else{
                footViewData.push(item);
            }
        });
        excelAllView.foot =  footViewData;

        //和head一样，将值填入foot中
        if(excelAllData.length > 0) {
           /* $.each(excelAllData[0], function (key, val) {
                $.each(excelAllView.foot, function (index, item) {//若是数据中的key与view.head的csof_code匹配，那么将值填入
                    if(item.CSOF_CODE == key) {
                        item.CELL_TXT = val;
                    }
                });
            });*/
        	$.each(excelAllView.foot, function(index, item) {
        		if(excelAllDataObj[item.SHEET_ID]) {
        			$.each(excelAllDataObj[item.SHEET_ID][0], function(key, val) {
        				if(item.CSOF_CODE == key) {
                            item.CELL_TXT = val;
                        }
        			});
        		}
        	});
        }

        /* 构建表尾 */
        $.each(pageArr, function (index1, item1) {
            var dataArr = [];//根据SHEET_ID分出的数据数组
            $.each(excelAllView.foot, function (index2, item2) {
                if(item2.SHEET_ID == item1) {
                    dataArr.push(item2);
                }
            });
            commonFn.buildTable(dataArr, item1, 'tfoot');//实现构建表内容
        });
    };
    

    /* 可编辑 */
    $('#myTabContent').on('click', 'td', function () {
        var text =  $(this).text();
        if(text == '-') {//将后台传过来的数据'-'变为''
            text = '';
        }
        if(isEdit){//判断是否点击编辑按钮
            if($(this).find('input').val() != undefined){
                return;
            }else{
                if($(this).data('edit') == '1'){//根据后台数据判断是否是可编辑状态
                    var type = commonFn.findType($(this).data('type')).type;
                    var html = '<input type="'+ type +'" value="'+ text +'" onkeyup="verifyInput(this,\'30\')" />';
                    if(type == 'money'){//若后台编辑类型为money，则显示如下类型
                        var html = '<input type="number"  aria-label="Amount (to the nearest dollar)" value="'+ Number(text.split(',').join('')) +'" max="1000000000000000" min="0"/>';
                    }
                    if(type == 'precent'){//若后台编辑类型为precent，则显示如下类型
                        var html = '<input type="text"  value="'+ Number(text.split('%')[0])/100 +'" onkeyup="verifyInput(this,\'30\')"/>';
                    }
                    $(this).html(html);
                    $('input').focus();//给input自动获取焦点
                }else{
                    ip.ipInfoJump('表单不能编辑','info');
                }
            }
        }
    });

    /* 失去焦点去除输入框 */
    $('#myTabContent').on('blur', 'input', function () {
        if($(this).parent().data('type') == '#M'){
            $(this).parent().text(commonFn.toThousands($(this).val()));//将数字转化为千分符号显示
        }else if($(this).parent().data('type') == '#P'){
            $(this).parent().text(commonFn.toPrecent($(this).val()));//将数字转化为百分比数
        }else{
            var val = $(this).val();
            $(this).parent().html('')
                .text(val);
        }
    });

    /* 编辑 */
    $('#edit').click(function () {
        isEdit = true;
        $('td[data-edit="1"]').addClass('editBg');
        $('td[data-edit="0"]').addClass('noeditBg');
        $('#cancelEdit').removeAttr('disabled');
        $('#edit').attr('disabled', 'disabled');
        $('#addOne').removeAttr('disabled', 'disabled');
        //$('#deleteOne').removeAttr('disabled', 'disabled');
        $('#saveExc').removeAttr('disabled', 'disabled');
        //$('td').addClass('editBg');
    });

    /* 取消编辑 */
    $('#cancelEdit').click(function () {
        isEdit = false;
        /* 给是否可编辑状态添加样式 */
        $('td[data-edit="1"]').removeClass('editBg');
        $('td[data-edit="0"]').removeClass('noeditBg');

        /* 改变按钮是否可用状态 */
        $('#edit').removeAttr('disabled');
        $('#cancelEdit').attr('disabled', 'disabled');
        $('#addOne').attr('disabled', 'disabled');
        //$('#deleteOne').attr('disabled', 'disabled');
    });

    /* 导入 */
    $('#loadIn').click(function () {
        $('#uploadModal').modal('show');
        $("#fileUpload")[0].src = "";
		var fileData = {
				"entity_id":entity_id,
				"oid":options.svOfficeId,
				"dep_id":options.svDepId,
				"dep_code":options.svDepCode,
				"edit":admin,
				"modular": modular
		    };
        $("#fileUpload")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
    	"&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&modular="+fileData.modular+"&entityName=we_are_different&oid="+fileData.oid+
    	"&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit;
    });

    /* 导人数据 */
    $('#loadDataIn').click(function () {
        $('#loadDataModal').modal('show');
        $("#loadData")[0].src = "";
        var fileData = {
            "entity_id":entityId,
            "oid":options.svOfficeId,
            "dep_id":options.svDepId,
            "dep_code":options.svDepCode,
            "edit":admin,
			"modular": modular
        };
        $("#loadData")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
            "&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&modular="+fileData.modular+"&entityName=we_are_different&oid="+fileData.oid+
            "&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit;
    });


    /* 导出 */
    $('#loadOut').click(function () {
        $.post(requireUrl.GET_EXCEL,{viewId:useviewId}, function (map) {
            if(map.errorCode == '0') {
                excelAllView = map.view;
                $.each(excelAllView, function (index, item) {
                    item = item.sort(commonFn.sortByPro('ROW_INDEX'));
                });

                //对数据在以ROW_INDEX排序的前提下在按照COL_INDEX进行排序
                /*
                * 思路：将已经按照row_index排序好的数据通过row_index进行分组形成sortObj = ｛ROW_INDEX1：[],ROW_INDEX2:[],ROW_INDEX3:[]｝
                * 这样的对象，通过遍历对对象进行col_index值的排序，再将排序好的对象按照顺序组合形成sortArr
                * 将sortArr的值赋值给遍历的某个对象如：body，foot，head
                * */
                $.each(excelAllView, function (key, val) {
                    var sortObj = {};
                    $.each(val, function (index1, item1) {
                        if(sortObj[item1.ROW_INDEX]) {
                            sortObj[item1.ROW_INDEX].push(item1);
                        } else {
                            sortObj[item1.ROW_INDEX] = [];
                            sortObj[item1.ROW_INDEX].push(item1);
                        }
                    });
                    var sortArr = [];
                    $.each(sortObj, function (key1, val1) {
                        val1 = val1.sort(commonFn.sortByPro('COL_INDEX'));
                        sortArr = sortArr.concat(val1);
                    });
                    excelAllView[key] = sortArr;
                });
//                showExcel();
                $.ajax({
                    type: 'POST',
                    url: requireUrl.GET_DATA,
                    contentType: 'application/json',
                    dataType: 'json',
                    data: JSON.stringify({viewId:useviewId,billId:billId}),
                    success: function (map) {
                    	 excelAllData = map.data;
                         showExcel();
                    }

                });
            } else {
                ip.ipInfoJump('数据查询失败！', 'info');
            }
        });
    });
    /* 保存 */
    $('#saveExc').click(function () {
        var allData = commonFn.getExcData();
        var data = allData.data;
        var viewHeadData = allData.view.head;
        var viewFootData = allData.view.foot;
        var postData = {};//传递到后台的数据

        //根据Head头部数据创建接收的数据格式
        $.each(viewHeadData, function (index1, item1) {
            if(postData[item1.SHEET_ID]) {
                //
            } else {
                postData[item1.SHEET_ID] = [];
            }
        });

        if(data.length == 0) { //判断要保存的body数据是否为空
            $.each(postData, function (key, val) {
                var dataObj = {};
                $.each(viewHeadData, function (index1, item1) {//遍历body的数据，将数据中所有的CSOF_CODE填入数据的每一层
                    if(item1.SHEET_ID == key && item1.IS_EDIT == '1') {
                        dataObj[item1.CSOF_CODE] = item1.CELL_TXT;
                    }
                });

                $.each(viewFootData, function (index1, item1) {//遍历foot的数据，将数据中所有的CSOF_CODE填入数据的每一层
                    if(item1.SHEET_ID == key && item1.IS_EDIT == '1') {
                        dataObj[item1.CSOF_CODE] = item1.CELL_TXT;
                    }
                });
                
                
                
                val.push(dataObj);
            });
        }else { //保存数据中的数据不为空时
            $.each(data, function (index, item) {
                //判断data.sheet_id是否存在，若存在，直接将数据存储到想对于的数据中，若不存在，创建一个数据[]，再将数据push进去
                if(postData[item.SHEET_ID]) {
                    postData[item.SHEET_ID].push(item);
                } else {
                    postData[item.SHEET_ID] = [];
                    postData[item.SHEET_ID].push(item);
                }
            });

            $.each(postData, function (key, val) {
                var dataObj = {};
                $.each(viewHeadData, function (index1, item1) {//遍历body的数据，将数据中所有的CSOF_CODE填入数据的每一层
                    if(item1.SHEET_ID == key && item1.IS_EDIT == '1') {
                        if(val.length > 0) {
                            $.each(val, function (index2, item2) {//找到匹配的sheetID将数据填入对应的sheet中
                                item2[item1.CSOF_CODE] = item1.CELL_TXT;
                            });
                        }else {
                            dataObj[item1.CSOF_CODE] = item1.CELL_TXT;
                        }
                    }
                });

                $.each(viewFootData, function (index1, item1) {//遍历foot的数据，将数据中所有的CSOF_CODE填入数据的每一层
                    if(item1.SHEET_ID == key && item1.IS_EDIT == '1') {
                        if(val.length > 0) {
                            $.each(val, function (index2, item2) {//找到匹配的sheetID将数据填入对应的sheet中
                                item2[item1.CSOF_CODE] = item1.CELL_TXT;
                            });
                        }else {
                            dataObj[item1.CSOF_CODE] = item1.CELL_TXT;
                        }
                    }
                });
                if(commonFn.isEmptyObj(dataObj)) {
                    return;
                }else {
                    val.push(dataObj);
                }
            });
        }
        /*将数据中的'-'装换为''*/
        $.each(postData, function(key, val) {
        	$.each(val, function(index, item) {
        		$.each(item, function(key2, val2) {
        			if(val2 == '-' || val2 == '-.00') {
                        item[key2] = '';
        			}
        		});
        	});
        });
        
      //判断数据是否全为空或未填，若未填则不加，否则加上sheet_id
        $.each(postData, function(key, val) {
        	$.each(val, function(index, item) {
        		var needAddSheetId = 0;
        		$.each(item, function(key2, val2) {
        			if(val2 != '') {
        				needAddSheetId++;
        			}
        		});
        		if(needAddSheetId > 0) {
        			item.SHEET_ID = key;
                }
        	});
        });
       /* $.each(dataObj, function(key, val) {
        	if (val != '-' && val != '') {
        		needAddSheetId++;
        	}
        });
        if(needAddSheetId > 0) {
        	dataObj.SHEET_ID = key;
        }*/
        
        $.ajax({
            type: 'POST',
            url: requireUrl.SAVE_EXCEL,
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify({mapParam:postData,billtypeCode:billtypeCode ,billId:billId ,objtypeId:objtypeId ,objId:objId ,supCycle:supCycle,supDate:supDate}),
            success: function (map) {
                $('#saveKeepVal').val(map.errorCode);
                if(map.errorCode == 0){
                	ip.ipInfoJump('保存成功！', 'info');
                	$('#saveKeepVal').val('1');
                	$('#saveExc').attr('disabled', 'disabled');
                	$('#edit').removeAttr('disabled', 'disabled');
                	$('#cancelEdit').click();
                }else if(map.errorCode == 1){
                	ip.ipInfoJump('保存失败！', 'info');
                	$('#saveKeepVal').val('0');
                	$('#saveExc').removeAttr('disabled', 'disabled');
                	$('#edit').click();
                }
            }

        });
    });

    /* 新增 */
    $('#addOne').click(function () {
        var id = $('#myTabContent').find('.active').attr('id');
        var arr = [];

        //新增之前获取所有已经填写的数据，
        excelAllData = commonFn.getExcData().data;

        $.each(excelAllData, function (index, item) {
            if(item.SHEET_ID == id){
                arr.push(item);
            }
        });
        var moduleArr = $.extend(true, {}, arr[arr.length - 1]);
        $.each(moduleArr, function (key, val) {
            if(key == 'ROW_INDEX'){
                moduleArr[key] = Number(val) + 1 + '';
            }
            if(key == 'id'){
                function addZero(num){//补零 1 => '01'  11 => '11'
                    if(num < 10){
                        return '0' + num;
                    }else{
                        return num + '';
                    }
                }
                moduleArr[key] = val.split('-')[0] + '-' + addZero(Number(val.split('-')[1]) + 1);

            }
            if(key.indexOf('C') > -1){
                moduleArr[key] = '';
            }
        });


        var num = 0;//设置一个变量记录
        $.each(excelAllData, function (index, item) {//遍历此时的excelAllData，判断是否
           if(item.SHEET_ID == id) {
               num ++;
           }
        });
        if(num == 0) {
            excelAllData = excelAllData.concat([commonFn.getBaseData(id, excelAllView.body)]);
        }else {
            excelAllData.push(moduleArr);
        }

        commonFn.buildBody(pageArr, excelAllView.body, excelAllData);
    });

    var getRowClickIndex;//获取点击行的index；
    /* 给点击行添加一个背景色 */
    $('#myTabContent').on('click', 'tbody tr', function () {
        $(this).addClass('clickBg').siblings().removeClass('clickBg');
        getRowClickIndex = $(this).data('index');
    });

    /* 删除行 */
    $('#deleteOne').click(function () {
        var id = $('#myTabContent').find('.active').attr('id');
        excelAllData = commonFn.getExcData().data;
        if(getRowClickIndex != undefined) {
            var deleteIndex = getRowClickIndex;//记录循环遍历得到与ROW_INDEX getRowClickIndex相同的值
            var excelAllDataObj = {};
            $.each(excelAllData, function(index, item) {
            	if (excelAllDataObj[item.SHEET_ID]) {
            		excelAllDataObj[item.SHEET_ID].push(item);
            	} else {
            		excelAllDataObj[item.SHEET_ID] = [];
            		excelAllDataObj[item.SHEET_ID].push(item);
            	}
            });
            excelAllDataObj[id].splice(deleteIndex - 1, 1);//根据索引删除点击的数据行
            var newDataArr = [];
            $.each(excelAllDataObj, function(key, val) {
            	newDataArr = newDataArr.concat(val);
            });
            excelAllData = newDataArr;
            commonFn.buildBody(pageArr, excelAllView.body, excelAllData);
        } else {
            ip.ipInfoJump('请选择需要删除的行！', 'info');
        }
        
    });

    /* 模态框选择文件确认按钮 */
    $('#uploadModal').find('.btn-primary').click(function () {
        var arr =  $('#fileUpload').contents().find('#fileList > div'),
            idArr = [],//存储得到的id数组
            viewId;//存储后台获取的viewId
        $.each(arr, function (index, item) {
           idArr.push(item.id);
        });
        if(idArr.length != 1) {//单笔导入
            ip.ipInfoJump('请勿导入多个文件！','info');
        } else {
            var name = $('#fileUpload').contents().find('#' + idArr[0]).find('.fileName').attr('title');
            if(name.split('.')[name.split('.').length - 1] == 'xls' || name.split('.')[name.split('.').length - 1] == 'xlsx') {
            	$.ajax({
                    type: 'POST',
                    url: requireUrl.SAVE_VIEW,
                    contentType: 'application/json',
                    dataType: 'json',
                    data: JSON.stringify({attachId: idArr[0],pViewId:pViewId}),
                    success: function (map) {
                        viewId = map.viewId;
                        $('#uploadModal').modal('hide');
                    }
                }).then(function () {//获取viewId的值后，通过后台查询相对于的viewId的excel数据
                    $.post(requireUrl.GET_EXCEL, {viewId: viewId}, function (map) {
                        if(map.errorCode == '0') {
                            excelAllView = map.view;
                            $.each(excelAllView, function (index, item) {
                                item = item.sort(commonFn.sortByPro('ROW_INDEX'));
                            });
                            //对数据在以ROW_INDEX排序的前提下在按照COL_INDEX进行排序
                            /*
                             * 思路：将已经按照row_index排序好的数据通过row_index进行分组形成sortObj = ｛ROW_INDEX1：[],ROW_INDEX2:[],ROW_INDEX3:[]｝
                             * 这样的对象，通过遍历对对象进行col_index值的排序，再将排序好的对象按照顺序组合形成sortArr
                             * 将sortArr的值赋值给遍历的某个对象如：body，foot，head
                             * */
                            $.each(excelAllView, function (key, val) {
                                var sortObj = {};
                                $.each(val, function (index1, item1) {
                                    if(sortObj[item1.ROW_INDEX]) {
                                        sortObj[item1.ROW_INDEX].push(item1);
                                    } else {
                                        sortObj[item1.ROW_INDEX] = [];
                                        sortObj[item1.ROW_INDEX].push(item1);
                                    }
                                });
                                var sortArr = [];
                                $.each(sortObj, function (key1, val1) {
                                    val1 = val1.sort(commonFn.sortByPro('COL_INDEX'));
                                    sortArr = sortArr.concat(val1);
                                });
                                excelAllView[key] = sortArr;
                            });
                            showExcel();
                        } else {
                            ip.ipInfoJump('数据查询失败！', 'info');
                        }
                    });
                });
            } else {
                ip.ipInfoJump('请导入一个excel文件！','info');
            }
        }
    });
    
    /* 导入数据按钮 */
   $('#loadDataModal').find('.btn-primary').click(function () {
       var arr =  $('#loadData').contents().find('#fileList > div'),
           idArr = [],//存储得到的id数组
           viewId;//存储后台获取的viewId
       $.each(arr, function (index, item) {
           idArr.push(item.id);
       });
       if(idArr.length != 1) {//单笔导入
           ip.ipInfoJump('请勿导入多个文件！','info');
       } else {
           var name = $('#loadData').contents().find('#' + idArr[0]).find('.fileName').attr('title');
           if(name.split('.')[name.split('.').length - 1] == 'xls' || name.split('.')[name.split('.').length - 1] == 'xlsx') {
               $.ajax({
                   type: 'POST',
                   url: requireUrl.SAVE_EXCEL_DATA,
                   contentType: 'application/json',
                   dataType: 'json',
                   data: JSON.stringify({attachId: idArr[0], viewId: viewId, billtypeCode:billtypeCode ,billId:billId ,objtypeId:objtypeId ,objId:objId ,supCycle:supCycle,supDate:supDate}),
                   success: function (map) {
                   }
               });
           } else {
               ip.ipInfoJump('请导入一个excel文件！','info');
           }
       }
   });
   
   initExcel = function() {
	   $.post(requireUrl.GET_EXCEL,{viewId:useviewId}, function (map) {
            if(map.errorCode == '0') {
                excelAllView = map.view;
                $.each(excelAllView, function (index, item) {
                    item = item.sort(commonFn.sortByPro('ROW_INDEX'));
                });

                //对数据在以ROW_INDEX排序的前提下在按照COL_INDEX进行排序
                /*
                * 思路：将已经按照row_index排序好的数据通过row_index进行分组形成sortObj = ｛ROW_INDEX1：[],ROW_INDEX2:[],ROW_INDEX3:[]｝
                * 这样的对象，通过遍历对对象进行col_index值的排序，再将排序好的对象按照顺序组合形成sortArr
                * 将sortArr的值赋值给遍历的某个对象如：body，foot，head
                * */
                $.each(excelAllView, function (key, val) {
                    var sortObj = {};
                    $.each(val, function (index1, item1) {
                        if(sortObj[item1.ROW_INDEX]) {
                            sortObj[item1.ROW_INDEX].push(item1);
                        } else {
                            sortObj[item1.ROW_INDEX] = [];
                            sortObj[item1.ROW_INDEX].push(item1);
                        }
                    });
                    var sortArr = [];
                    $.each(sortObj, function (key1, val1) {
                        val1 = val1.sort(commonFn.sortByPro('COL_INDEX'));
                        sortArr = sortArr.concat(val1);
                    });
                    excelAllView[key] = sortArr;
                });
//                showExcel();
                $.ajax({
                    type: 'POST',
                    url: requireUrl.GET_DATA,
                    contentType: 'application/json',
                    dataType: 'json',
                    data: JSON.stringify({viewId:useviewId,billId:billId}),
                    success: function (map) {
                    	 excelAllData = map.data;
                         showExcel();
                    }

                });
            } else {
                ip.ipInfoJump('数据查询失败！', 'info');
            }
        });
   }

    function init(){ //整个JS代码初始化
        app = u.createApp({
            el: 'body',
            model: viewModel
        });
        initExcel();
    }
    init();
});