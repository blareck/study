DataGrid = function() {
	this.element = null;
	this.id = null;
	this.dataSource = null;
	this.columns = null;
	this.maxCount = 10;
	this.start = 1;
	this.total = 0;
	this.multiple = false;
	this.selectable = true;
	this.showPager = true;
	this.initialized = false;
	this.showNumberColumn = true;
	this.numberColumnCss = {width:"50px"};

	this.setDataSource = function(dataSource) {
		this.dataSource = dataSource;
	};

	this.setStart = function(start) {
		this.start = start;
	};

	this.setMaxCount = function(maxCount) {
		this.maxCount = maxCount;
	};
	
	this.setTotal = function(total){
		this.total = total;
	};

	this.setNumberColumnCss = function(css) {
		this.numberColumnCss = css;
	};

	this.create = function(id) {
		this.id = id;
		this.element = $("<div>").addClass("DataGrid").attr("id",
				this.id + "_div1").get(0);
		$("#" + id).empty().append(this.element);
		//创建分页按钮条
		 $("<div>").addClass("DataGrid-PagerTab").attr("id",this.id+"_pagerTab")
		 	.append($("<SPAN>").attr("id",this.id+"_page_count")).appendTo(this.element);
		//创建数据展示的表格
		$("<TABLE>").addClass("DataGrid-table").attr("id", this.id + "_resultTable")
					.append($("<THEAD>")).append("<TBODY>").append("<TFOOT>").appendTo(this.element);
	};

	/**
	 * 初始化表头
	 */
	this.setColumns = function(columns) {
		this.columns = columns;
		var headElement = $("TABLE THEAD",this.element);
		headElement.children().remove();

		var headRow = null;
		headRow = $("<TR>").addClass("DataGrid-thead-row");

		if (this.showNumberColumn) {
			$("<TD>").html("序号").css(this.numberColumnCss).addClass(
					"DataGrid-thead-cell").appendTo(headRow);
		}
		headRow.appendTo(headElement);

		for ( var i = 0; i < this.columns.length; i++) {
			var css = this.columns[i].css || {};
			var head = $("<TD>", {
				columnIndex : i
			});
			head.html(this.columns[i].label).css(css).addClass(
					"DataGrid-thead-cell").appendTo(headRow);

			this.columns[i].editorAdapter = $.extend({
				onRender : function(event) {
					var temp = event.object[event.column.field];
					if (typeof (temp) == "undefined") {
						return {};
					} else {
						return temp;
					}
				},
				onCss : function(event){
					var temp = event.column.css;
					if (typeof (temp) == "undefined") {
						return {};
					} else {
						return temp;
					}
				},
				onAttribute : function(event){
					var temp = event.column.attr;
					if (typeof (temp) == "undefined") {
						return {};
					} else {
						return temp;
					}
				}
			}, this.columns[i].editorAdapter || {});
		}
		initPagerButton($("#"+this.id+"_pagerTab"));
	};
	
	this.first = function() {
		this.disableButtons();
		this.setStart(1);

		this.processOnRequest({
			start : 1,
			count : this.maxCount
		});
	};

	this.clear = function() {
		this.disableButtons();
		this.setStart(1);
		this.count(0);
		this.setData([]);
	};

	this.last = function() {
		this.disableButtons();
		var left = this.total % this.maxCount;
		var start = this.total - (left == 0 ? this.maxCount : left) + 1;
		this.setStart(start);
		this.processOnRequest({
			start : start,
			count : this.maxCount
		});
	};

	this.disableButtons = function() {
		$("#"+this.element.id + "_pager_first").attr("disabled","disabled");
		$("#"+this.element.id + "_pager_previous").attr("disabled","disabled");
		$("#"+this.element.id + "_pager_next").attr("disabled","disabled");
		$("#"+this.element.id + "_pager_last").attr("disabled","disabled");
	};
	
	this.initPagerNumber = function(){
		var pagerTab = $("#"+this.id+"_page_count");
		var pageCount = Math.floor(this.total / this.maxCount)
				+ (source % this.maxCount == 0 ? 0 : 1);
		var pageCurrent = Math.floor(this.start / this.maxCount) + 1;
		pagerTab.text("第"+pageCurrent + "页/总计" + pageCount+"页");
		var nextButton = $("#"+this.id + "_pager_next");
		var previousButton = $("#"+this.id+ "_pager_previous");
		var firstButton = $("#"+this.id + "_pager_first");
		var lastButton = $("#"+this.id + "_pager_last");
		if (pageCount <= 1) {
			this.disableButtons();
		} else {
			if (pageCurrent == 1) {
				firstButton.attr("disabled","disabled");
				previousButton.attr("disabled","disabled");
				nextButton.removeAttr("disabled");
				lastButton.removeAttr("disabled");
			} else if (pageCurrent == pageCount) {
				firstButton.removeAttr("disabled");
				previousButton.removeAttr("disabled");
				nextButton.attr("disabled","disabled");
				lastButton.attr("disabled","disabled");
			} else {
				firstButton.removeAttr("disabled");
				previousButton.removeAttr("disabled");
				nextButton.removeAttr("disabled");
				lastButton.removeAttr("disabled");
			}
		}
	};
	
	/**
	 * 初始化分页按钮
	 * pagerTab 存放按钮的HTML元素，jQuery对象
	 */
	this.initPagerButton = function(pagerTab) {
		if (!this.initialized) {
			var firstButton = $("<INPUT>",{type:"button",value:"首页"})
				.addClass("DataGrid-PagerButton").attr("id",this.id + "_pager_first").appendTo(pagerTab);
			var previousButton = $("<INPUT>",{type:"button",value:"前页"})
				.addClass("DataGrid-PagerButton").attr("id",this.id + "_pager_previous").appendTo(pagerTab);
			var nextButton = $("<INPUT>",{type:"button",value:"后页"})
				.addClass("DataGrid-PagerButton").attr("id",this.id + "_pager_next").appendTo(pagerTab);
			var lastButton = $("<INPUT>",{type:"button",value:"末页"})
				.addClass("DataGrid-PagerButton").attr("id",this.id + "_pager_last").appendTo(pagerTab);

			this.disableButtons();

			var dataGrid = this;

			firstButton.addClickListener({
				onClick : function(event) {
					dataGrid.first();
				}
			});

			lastButton.addClickListener({
				onClick : function(event) {
					dataGrid.last();
				}
			});

			previousButton.addClickListener({
				onClick : function(event) {
					dataGrid.disableButtons();
					var start = dataGrid.start - dataGrid.maxCount;
					dataGrid.setStart(start);
					dataGrid.processOnRequest({
						start : start,
						count : dataGrid.maxCount
					});
				}
			});
			
			nextButton.addClickListener({
				onClick : function(event) {
					dataGrid.disableButtons();
					var start = dataGrid.start + dataGrid.maxCount;
					dataGrid.setStart(start);
					dataGrid.processOnRequest({
						start : start,
						count : dataGrid.maxCount
					});
				}
			});
			
			initialized = true;
		}
	};

	
	this.setData = function(data){
		var bodyProxy = $("TABLE TBODY", this.element);
		bodyProxy.children().remove();
		for ( var i = 0; i < data.length; i++) {
			var rowProxy = $("<TR>", {
				id : this.id + "_row_" + i,
				index : i
			}).addClass("DataGrid-row");
			if (this.showNumberColumn) {
				$("<TD>").html(i + 1).addClass("DataGrid-cell").appendTo(
						rowProxy);
			}
			rowProxy.appendTo(bodyProxy);
			for ( var h = 0; h < this.columns.length; h++) {
				var cellProxy = $("<TD>", {
					id : this.id + "_row_" + i + "_cell_" + h
				}).addClass("DataGrid-cell");
				cellProxy.appendTo(rowProxy);
				this.generateCell(i, cellProxy, this.columns[h], data[i]);
			}
		}
	};
	
	this.generateCell = function(index, cellProxy, column, bean) {
		var adapterEvent = {
			index : index,
			object : bean,
			column : column,
			cell : cellProxy
		};
		switch (column.type) {
			case "text": {
				$("<SPAN>").css(
						column.editorAdapter.onCss(adapterEvent)).html(
						column.editorAdapter.onRender(adapterEvent)).attr(column.editorAdapter.onAttribute(adapterEvent))
						.appendTo(cellProxy);
				break;
			}
			case "link": {
				$("<A>", $.extend({
					href : column.url(adapterEvent)
				}, column.editorAdapter.onAttribute(adapterEvent))).css(
						column.editorAdapter.onCss(adapterEvent)).html(
						column.editorAdapter.onRender(adapterEvent)).appendTo(
						cellProxy);
				break;
			}
			case "button": {
				var b = $("<INPUT>", {
					value : column.editorAdapter.onRender(adapterEvent),
					type : "button"
				}).css(column.editorAdapter.onCss(adapterEvent)).bind(
						"click",
						function(event) {
							column.editorAdapter.onClick($.extend({},
									adapterEvent, {
										element : event.target
									}));
						}).appendTo(cellProxy);
				b.attr(column.editorAdapter.onAttribute(adapterEvent));
				break;
			}
			case "linkbutton": {
				$("<A>", $.extend({
					href : "#"
				}, column.editorAdapter.onAttribute(adapterEvent))).css(
						column.editorAdapter.onCss(adapterEvent)).html(
						column.editorAdapter.onRender(adapterEvent)).bind(
						"click", function(event) {
							column.editorAdapter.onClick(adapterEvent);
						}).appendTo(cellProxy);
				break;
			}
			default: {
				cellProxy.html("");
				break;
			}
		}
	};
	
	this.processOnRequest = function(event){
		var dataGrid = this;
		this.setStart(event.start);
		dataSource.getData({
			start : event.start,
			maxCount : event.maxCount,
			initPagerNumber : function(event){
				dataGrid.setTotal(event.total);
				initPagerNumber();
			},
			setData : function(event){
				this.setData(event.data);
			}
		});
	};
	
	this.refresh = function(){
		this.processOnRequest({start : this.start,maxCount : this.maxCount});
	};
};

DataSource = function(){
	this.parameters = new Array();
	this.ajaxURL = null;
	this.currentPage = 1;
	this.maxCount = 10;
	this.total = 0;
	this.requestParameters = {};
	
	this.setParameter = function(name, value) {
		this.parameters[name] = value;
	};

	this.getParameter = function(name) {
		return this.parameters[name];
	};
	
	this.setAjaxURL = function(url){
		this.ajaxURL = url;
	};
	
	this.setTotal = function(total){
		this.total = total;
	};
	
	this.setMaxCount = function(maxCount){
		this.maxCount = maxCount;
	};
	
	this.setCurrentPage = function(currentPage){
		this.currentPage = currentPage;
	};
	
	this.setRequestParameters = function(requestParameters){
		this.requestParameters = requestParameters||{};
	};
	
	this.getData = function(event){
		//this.setParameter("maxCount", event.maxCount);
		//this.setParameter("start",event.start);
		var dataSource = this;
		$.extend(this.requestParameters,{maxCount : event.maxCount});
		if(!!ajaxURL){
			$.getJSON(ajaxURL,requestParameters,function(data){
				dataSource.setTotal(data.total);
				event.initPagerNumber({total : data.total});
				event.setData(data);
			});
		}
	};
};
