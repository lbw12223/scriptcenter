# !/usr/bin/env python
# -*- coding: utf-8 -*-
import time

from util.Time import Time
import datetime
from util.jrutils import get_sql_password
from db.Hive import Hive
import logging
import sys
import re
import conf.conf as conf
from conf.env_conf import EnvConf
from util import common_http_gravity

from util import common_util

from db.DBI import DBI, execute_sql

from db.Spark import SparkSQL
from util.job_param_util import JobParam as jp
from util.job_param_util import JobParamConst
from util.job_parallel_util import JobParallel

"""
本模板为python sql任务模板
"""
logging.basicConfig(level=logging.INFO,
                    format='[%(asctime)s] [%(levelname)s] %(message)s',
                    # format='%(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S',
                    filemode='w')
########################################################################################################################
#  Creator          :
#  Creation Time    :
#  Description      :
#  Modified By      :
#  Modified Time    :
#  Modified Content :
#  Script Version   :1.0.0
########################################################################################################################
"""
#############################变量 UDF 说明部分############################################################# 
1.日期变量及其对应的日期格式(以下变量仅在SQL语句中有效)
以TX_DATE=2018-09-12为例，以下是各变量的取值

    TX_DATE: 2018-09-12,
    TXDATE: 20180912,
    TXPREMONTHLASTDATE: 20180831,
    TXMONTH: 201809,
    TX_MONTH: 2018-09,
    TXPREMONTHFIRSTDATE: 20180801,
    TX_PRE_6_DATE: 2018-09-06,
    TXMONTHLASTDATE: 20180930,
    TXPRE6DATE: 20180906,

    TXYEAR: 2018,
    TX_PREDATE: 2018-09-11,
    TXNEXTDATE: 20180913,
    TX_PREMONTHLASTDATE: 2018-08-31,
    TX_MONTHLASTDATE: 2018-09-30,
    TXYEARFIRSTDATE: 20180101,
    TXPREDATE: 20180911,
    TX_NEXTDATE: 2018-09-13,
    TXYEARWEEKNUM: 38,
    TXYEARLASTDATE: 20181231,
    TXMONTHFIRSTDATE: 20180901,
    TX_YEARFIRSTDATE: 2018-01-01,
    TX_YEARLASTDATE: 2018-12-31,
    TXPREYEAR: 2017,
    TX_PREMONTH: 2018-08,
    TX_MONTHFIRSTDATE: 2018-09-01,
    TX_PREMONTHFIRSTDATE: 2018-08-01,
    TXPREMONTH: 201808,

    TX_HOUR:23
    TX_MINUTE:59
    对于部分作业需要精确到时分，以上两个变量可以分别取得小时 （0-23） 和分钟 (0-59)

2.加解密秘钥
    key：加密秘钥

3. 常用UDF函数
    sysdate：获取系统日期，sysdate():当前日期 sysdate(plusDay:int):当前日期+plusDay
    changedate：日期函数，changedate():当前日期 changedate(plusDay:int):当前日期+plusyDay changedate('YYYY-MM-DD':string,plusDay:int):指定日期+plusyDay
    time2est：转换为东部时间(-13)，time2est('YYYY-MM-DD HH:mm:ss':string)
    date_format：日期格式化，date_format('date','current_format','convert_format')
    month_add：月份计算，month_add('YYYY-MM-DD',plusMonth)
    to_quarter：获取季度，to_quarter('YYYY-MM-DD':string)
    to_month：获取月份，to_month('YYYY-MM-DD':string)
    to_year：获取年，to_year('YYYY-MM-DD':string)
    get_pro：k-v对处理函数，get_pro('k1:v1^k2:v2':string,arg:string);arg=1时返回values,arg=0时返回keys
    rep_ascii：过滤特殊字符，rep_scii(str:string)
    getlasttwo：以\.分割取后两位，getlasttwo(str:string)
    idcardnotrans：15位身份证转18位，idcardnotrans('cardNo':string)
    padd：字符串填充，padd(str,填充到几位,填充值,是否前方填充)
    notationtrans：科学计数法转浮点数，notationtrans('科学计数法字符串')
    clean_char：去除左右非字母数字下划线字符，clean_char(str:string)
    getWeekDay：下一个周日日期，getWeekDay('YYYY-MM-DD')
    idNo_province：身份证查询所在省份，idNo_province(身份证字符串)查询所在省份
    idNo_sex：身份证查询性别，idNo_sex(身份证字符串)查询性别
    idNo_star：身份证查询星座，idNo_star(身份证字符串)查询星座
    idNo_birthday：身份证查询生日，idNo_birthday(身份证字符串)查询生日
    distanceCalculator：距离计算，distanceCalculator(lat1:double,lon1:double,lat2:double,lon2:double)
    IPDistrictExt：ip地址信息获取，IPDistrictExt(ipLstring,arg:string): args:{all,contry,province,city,county,districtCode,coordinate}
    IPExt：ip地址信息获取，IPDistrictExt(ipLstring,arg:string): args:{all,contry,province,city,county,districtCode,coordinate}
    substr_byte：截取字符串并转换utf-8编码，substr_byte(str:string,startlen:int,endlen:int)
    getmd5：获取md5，getmd5(str:string)
    详见https://cf.jd.com/pages/viewpage.action?pageId=109413236

#####################################################################################################################
"""

"""
#####################################################################################################################
sql编写区，多个sql按 SQL_BUFF1,SQL_BUFF2,SQL_BUFF3...新建，按数字下标顺序运行,需要几个sql就使用几个SQL_BUFFx变量
在sql中可以使用大量的日期变量，例如：
select * from {table1}  where dt='{tx_date}'
变量和别名一样，需要用{}括起来，日期变量的所有日期都是根据当前任务数据日期计算而来，如果当前的数据日期为：2017-10-09
则上述语句程序解析后，实际运行语句为： select * from tmp.tmp_a_payment_1 where dt='2017-10-09'
日期变量列表见日期变量列表说明

SQL编写注意事项：
1. 字段裁剪（只查询业务处理需要的字段）
2. 分区裁剪（查询条件限定分区字段，对应的比较值要求是固定条件，不能使用表字段或者执行时取值函数）
3. 查询条件尽早过滤（关联条件前置为表过滤条件，空值过滤）
4. 关联表时关联条件使用表唯一字段（关联表数据提前去重，避免使用多对多关联；不同类型字段关联时转换成同类型关联，大小表关联时小表前置）
5. 关联表时倾斜数据可以使用map join
6. 需要多次查询的含数据日期以外查询条件的数据，创建临时表保存数据
7. group by替代distinct
8. max(struct())替代rownumber()=1
9. muti-insert（一次查询结果插入多个表/分区）

SQL编写规范：
1. 注明任务名称
  set mapreduce.job.name=jobname_cusname_seq;
  use xxdb;
2. 不允许CREATE/DROP非临时表，不允许TRUNCATE/RENAME
3. 不允许INSERT临时表和目标表以外的表
#####################################################################################################################
"""

GRAVITY_URL = EnvConf.get('HTTP_URL', 'GRAVITY_SERVICE')
CHANNEL_URL = EnvConf.get('HTTP_URL', 'CHANNEL_SY')
IS_MOCK = EnvConf.get('ENV', 'IS_MOCK')

RUNNER_SPARK_SQL = "spark-sql"
RUNNER_HIVE = "hive"
RUNNER_STINGER = "stinger"
JobParam = jp

# 最大并行数
MAX_PARALLEL = 3

# given_tx_date  {yyyy}-{mm}-{dd}
def get_times(given_tx_date, tx_time):
    TXDATE = Time.date_format(date=given_tx_date, sep='')
    TXPREDATE = Time.date_sub(date=TXDATE)
    TXPRE6DATE = Time.date_sub(date=TXDATE, itv=6)
    TXNEXT6DATE = Time.date_sub(date=TXDATE, itv=-6)
    TXNEXTDATE = Time.date_add(date=TXDATE)

    TXMONTH = Time.year_month(tm=TXDATE, sep='')
    TXMONTHFIRSTDATE = Time.month_first_date(tm=TXDATE, sep='')
    TXMONTHLASTDATE = Time.month_last_date(tm=TXDATE, sep='')

    TXPREMONTH = Time.year_month_sub(ym=TXMONTH)
    TXPREMONTHFIRSTDATE = Time.month_first_date(tm=TXPREMONTH, sep='')
    TXPREMONTHLASTDATE = Time.month_last_date(tm=TXPREMONTH, sep='')

    TXYEARFIRSTDATE = Time.year_first_date(tm=TXDATE, sep='')
    TXYEARLASTDATE = Time.year_last_date(tm=TXDATE, sep='')

    TXYEAR = Time.year(tm=TXDATE)
    TXPREYEAR = Time.pre_year(tm=TXDATE)
    TXYEARWEEKNUM = Time.year_week(tm=TXDATE)

    TX_DATE = Time.date_format(date=given_tx_date)
    TX_PREDATE = Time.date_sub(date=TX_DATE)
    TX_PRE_60_DATE = Time.date_sub(date=TX_DATE, itv=60)
    TX_PRE_59_DATE = Time.date_sub(date=TX_DATE, itv=59)
    TX_PRE_58_DATE = Time.date_sub(date=TX_DATE, itv=58)
    TX_PRE_57_DATE = Time.date_sub(date=TX_DATE, itv=57)
    TX_PRE_56_DATE = Time.date_sub(date=TX_DATE, itv=56)
    TX_PRE_55_DATE = Time.date_sub(date=TX_DATE, itv=55)
    TX_PRE_54_DATE = Time.date_sub(date=TX_DATE, itv=54)
    TX_PRE_53_DATE = Time.date_sub(date=TX_DATE, itv=53)
    TX_PRE_52_DATE = Time.date_sub(date=TX_DATE, itv=52)
    TX_PRE_51_DATE = Time.date_sub(date=TX_DATE, itv=51)
    TX_PRE_50_DATE = Time.date_sub(date=TX_DATE, itv=50)
    TX_PRE_49_DATE = Time.date_sub(date=TX_DATE, itv=49)
    TX_PRE_48_DATE = Time.date_sub(date=TX_DATE, itv=48)
    TX_PRE_47_DATE = Time.date_sub(date=TX_DATE, itv=47)
    TX_PRE_46_DATE = Time.date_sub(date=TX_DATE, itv=46)
    TX_PRE_45_DATE = Time.date_sub(date=TX_DATE, itv=45)
    TX_PRE_44_DATE = Time.date_sub(date=TX_DATE, itv=44)
    TX_PRE_43_DATE = Time.date_sub(date=TX_DATE, itv=43)
    TX_PRE_42_DATE = Time.date_sub(date=TX_DATE, itv=42)
    TX_PRE_41_DATE = Time.date_sub(date=TX_DATE, itv=41)
    TX_PRE_40_DATE = Time.date_sub(date=TX_DATE, itv=40)
    TX_PRE_39_DATE = Time.date_sub(date=TX_DATE, itv=39)
    TX_PRE_38_DATE = Time.date_sub(date=TX_DATE, itv=38)
    TX_PRE_37_DATE = Time.date_sub(date=TX_DATE, itv=37)
    TX_PRE_36_DATE = Time.date_sub(date=TX_DATE, itv=36)
    TX_PRE_35_DATE = Time.date_sub(date=TX_DATE, itv=35)
    TX_PRE_34_DATE = Time.date_sub(date=TX_DATE, itv=34)
    TX_PRE_33_DATE = Time.date_sub(date=TX_DATE, itv=33)
    TX_PRE_32_DATE = Time.date_sub(date=TX_DATE, itv=32)
    TX_PRE_31_DATE = Time.date_sub(date=TX_DATE, itv=31)
    TX_PRE_30_DATE = Time.date_sub(date=TX_DATE, itv=30)
    TX_PRE_29_DATE = Time.date_sub(date=TX_DATE, itv=29)
    TX_PRE_28_DATE = Time.date_sub(date=TX_DATE, itv=28)
    TX_PRE_27_DATE = Time.date_sub(date=TX_DATE, itv=27)
    TX_PRE_26_DATE = Time.date_sub(date=TX_DATE, itv=26)
    TX_PRE_25_DATE = Time.date_sub(date=TX_DATE, itv=25)
    TX_PRE_24_DATE = Time.date_sub(date=TX_DATE, itv=24)
    TX_PRE_23_DATE = Time.date_sub(date=TX_DATE, itv=23)
    TX_PRE_22_DATE = Time.date_sub(date=TX_DATE, itv=22)
    TX_PRE_21_DATE = Time.date_sub(date=TX_DATE, itv=21)
    TX_PRE_20_DATE = Time.date_sub(date=TX_DATE, itv=20)
    TX_PRE_19_DATE = Time.date_sub(date=TX_DATE, itv=19)
    TX_PRE_18_DATE = Time.date_sub(date=TX_DATE, itv=18)
    TX_PRE_17_DATE = Time.date_sub(date=TX_DATE, itv=17)
    TX_PRE_16_DATE = Time.date_sub(date=TX_DATE, itv=16)
    TX_PRE_15_DATE = Time.date_sub(date=TX_DATE, itv=15)
    TX_PRE_14_DATE = Time.date_sub(date=TX_DATE, itv=14)
    TX_PRE_13_DATE = Time.date_sub(date=TX_DATE, itv=13)
    TX_PRE_12_DATE = Time.date_sub(date=TX_DATE, itv=12)
    TX_PRE_11_DATE = Time.date_sub(date=TX_DATE, itv=11)
    TX_PRE_10_DATE = Time.date_sub(date=TX_DATE, itv=10)
    TX_PRE_9_DATE = Time.date_sub(date=TX_DATE, itv=9)
    TX_PRE_8_DATE = Time.date_sub(date=TX_DATE, itv=8)
    TX_PRE_7_DATE = Time.date_sub(date=TX_DATE, itv=7)
    TX_PRE_6_DATE = Time.date_sub(date=TX_DATE, itv=6)
    TX_PRE_5_DATE = Time.date_sub(date=TX_DATE, itv=5)
    TX_PRE_4_DATE = Time.date_sub(date=TX_DATE, itv=4)
    TX_PRE_3_DATE = Time.date_sub(date=TX_DATE, itv=3)
    TX_PRE_2_DATE = Time.date_sub(date=TX_DATE, itv=2)
    TX_PRE_1_DATE = Time.date_sub(date=TX_DATE, itv=1)
    TX_NEXT_6_DATE = Time.date_sub(date=TX_DATE, itv=-6)
    TX_NEXT_5_DATE = Time.date_sub(date=TX_DATE, itv=-5)
    TX_NEXT_4_DATE = Time.date_sub(date=TX_DATE, itv=-4)
    TX_NEXT_3_DATE = Time.date_sub(date=TX_DATE, itv=-3)
    TX_NEXT_2_DATE = Time.date_sub(date=TX_DATE, itv=-2)
    TX_NEXT_1_DATE = Time.date_sub(date=TX_DATE, itv=-1)
    TX_NEXT_60_DATE = Time.date_sub(date=TX_DATE, itv=60)
    TX_NEXT_59_DATE = Time.date_sub(date=TX_DATE, itv=-59)
    TX_NEXT_58_DATE = Time.date_sub(date=TX_DATE, itv=-58)
    TX_NEXT_57_DATE = Time.date_sub(date=TX_DATE, itv=-57)
    TX_NEXT_56_DATE = Time.date_sub(date=TX_DATE, itv=-56)
    TX_NEXT_55_DATE = Time.date_sub(date=TX_DATE, itv=-55)
    TX_NEXT_54_DATE = Time.date_sub(date=TX_DATE, itv=-54)
    TX_NEXT_53_DATE = Time.date_sub(date=TX_DATE, itv=-53)
    TX_NEXT_52_DATE = Time.date_sub(date=TX_DATE, itv=-52)
    TX_NEXT_51_DATE = Time.date_sub(date=TX_DATE, itv=-51)
    TX_NEXT_50_DATE = Time.date_sub(date=TX_DATE, itv=-50)
    TX_NEXT_49_DATE = Time.date_sub(date=TX_DATE, itv=-49)
    TX_NEXT_48_DATE = Time.date_sub(date=TX_DATE, itv=-48)
    TX_NEXT_47_DATE = Time.date_sub(date=TX_DATE, itv=-47)
    TX_NEXT_46_DATE = Time.date_sub(date=TX_DATE, itv=-46)
    TX_NEXT_45_DATE = Time.date_sub(date=TX_DATE, itv=-45)
    TX_NEXT_44_DATE = Time.date_sub(date=TX_DATE, itv=-44)
    TX_NEXT_43_DATE = Time.date_sub(date=TX_DATE, itv=-43)
    TX_NEXT_42_DATE = Time.date_sub(date=TX_DATE, itv=-42)
    TX_NEXT_41_DATE = Time.date_sub(date=TX_DATE, itv=-41)
    TX_NEXT_40_DATE = Time.date_sub(date=TX_DATE, itv=-40)
    TX_NEXT_39_DATE = Time.date_sub(date=TX_DATE, itv=-39)
    TX_NEXT_38_DATE = Time.date_sub(date=TX_DATE, itv=-38)
    TX_NEXT_37_DATE = Time.date_sub(date=TX_DATE, itv=-37)
    TX_NEXT_36_DATE = Time.date_sub(date=TX_DATE, itv=-36)
    TX_NEXT_35_DATE = Time.date_sub(date=TX_DATE, itv=-35)
    TX_NEXT_34_DATE = Time.date_sub(date=TX_DATE, itv=-34)
    TX_NEXT_33_DATE = Time.date_sub(date=TX_DATE, itv=-33)
    TX_NEXT_32_DATE = Time.date_sub(date=TX_DATE, itv=-32)
    TX_NEXT_31_DATE = Time.date_sub(date=TX_DATE, itv=-31)
    TX_NEXT_30_DATE = Time.date_sub(date=TX_DATE, itv=-30)
    TX_NEXT_29_DATE = Time.date_sub(date=TX_DATE, itv=-29)
    TX_NEXT_28_DATE = Time.date_sub(date=TX_DATE, itv=-28)
    TX_NEXT_27_DATE = Time.date_sub(date=TX_DATE, itv=-27)
    TX_NEXT_26_DATE = Time.date_sub(date=TX_DATE, itv=-26)
    TX_NEXT_25_DATE = Time.date_sub(date=TX_DATE, itv=-25)
    TX_NEXT_24_DATE = Time.date_sub(date=TX_DATE, itv=-24)
    TX_NEXT_23_DATE = Time.date_sub(date=TX_DATE, itv=-23)
    TX_NEXT_22_DATE = Time.date_sub(date=TX_DATE, itv=-22)
    TX_NEXT_21_DATE = Time.date_sub(date=TX_DATE, itv=-21)
    TX_NEXT_20_DATE = Time.date_sub(date=TX_DATE, itv=-20)
    TX_NEXT_19_DATE = Time.date_sub(date=TX_DATE, itv=-19)
    TX_NEXT_18_DATE = Time.date_sub(date=TX_DATE, itv=-18)
    TX_NEXT_17_DATE = Time.date_sub(date=TX_DATE, itv=-17)
    TX_NEXT_16_DATE = Time.date_sub(date=TX_DATE, itv=-16)
    TX_NEXT_15_DATE = Time.date_sub(date=TX_DATE, itv=-15)
    TX_NEXT_14_DATE = Time.date_sub(date=TX_DATE, itv=-14)
    TX_NEXT_13_DATE = Time.date_sub(date=TX_DATE, itv=-13)
    TX_NEXT_12_DATE = Time.date_sub(date=TX_DATE, itv=-12)
    TX_NEXT_11_DATE = Time.date_sub(date=TX_DATE, itv=-11)
    TX_NEXT_10_DATE = Time.date_sub(date=TX_DATE, itv=-10)
    TX_NEXT_9_DATE = Time.date_sub(date=TX_DATE, itv=-9)
    TX_NEXT_8_DATE = Time.date_sub(date=TX_DATE, itv=-8)
    TX_NEXT_7_DATE = Time.date_sub(date=TX_DATE, itv=-7)
    TX_NEXTDATE = Time.date_add(date=TX_DATE)

    TX_MONTH = Time.year_month(tm=TX_DATE, sep='-')
    TX_MONTHFIRSTDATE = Time.month_first_date(tm=TX_DATE)
    TX_MONTHLASTDATE = Time.month_last_date(tm=TX_DATE)

    TX_PREMONTH = Time.year_month_sub(ym=TX_MONTH)
    TX_PREMONTHFIRSTDATE = Time.month_first_date(tm=TX_PREMONTH)
    TX_PREMONTHLASTDATE = Time.month_last_date(tm=TX_PREMONTH)

    TX_YEARFIRSTDATE = Time.year_first_date(tm=TX_DATE)
    TX_YEARLASTDATE = Time.year_last_date(tm=TX_DATE)

    TX_HOUR = None
    TX_MINUTE = None
    if tx_time:
        TX_HOUR = tx_time[0:2]
        TX_MINUTE = tx_time[2:4]
    return locals()


def get_udf():
    en_sstv = "sstv_udf.dw_enaks"
    de_sstv = "sstv_udf.dw_deaks"
    tm_sstv = "sstv_udf.dw_deaks_tm"
    join_sstv = "sstv_udf.dw_deaks_join"
    mwtm_sstv = "sstv_udf.dw_tm"
    mwjoin_sstv = "sstv_udf.dw_join"
    return locals()


# XXX_JDW_DWD_INSU_BDDSBZ_GLOBALCATEGORY_S_D_20171014.dir
# XXX_JDW_DWD_INSU_BDDSBZ_GLOBALCATEGORY_S_D_201710142203.dir (yyyyMMddhhmm)
def get_job_name_and_tx_date(CONTROL_FILE):
    fields = CONTROL_FILE.split("_")

    job_name = "_".join(fields[1:-1])
    last_field = fields[-1]
    txdate_str = last_field.split(".")[0]
    # tx_date = "{yyyy}{mm}{dd}".format(yyyy=txdate_str[0:4],mm=txdate_str[4:6],dd=txdate_str[6:8])
    tx_date = "{yyyy}-{mm}-{dd}".format(yyyy=txdate_str[0:4], mm=txdate_str[4:6], dd=txdate_str[6:8])
    tx_time = None
    if len(txdate_str) == 10:
        tx_time = txdate_str[8:12]

    return job_name, tx_date, tx_time


def get_execute_engine(sql_runner):
    if sql_runner == RUNNER_SPARK_SQL:
        spark_sql = SparkSQL()
        return spark_sql
    else:
        hive = Hive()
        hive.set_hive_config(sql_runner)
        return hive


def init_pre_settings(sql_runner, execute_engine, export_list, cmdline_args):
    if sql_runner == RUNNER_SPARK_SQL:
        execute_engine.set_export_list(export_list)
        cmdline_args.add("--conf spark.debug.maxToStringFields=100")
        execute_engine.set_cmdline_args(cmdline_args)
    else:
        execute_engine.set_export_env(export_list)


class SqlTask(object):
    def __init__(self):
        self._sql_runner = RUNNER_SPARK_SQL
        self._export_list = set()
        self._cmdline_args = set()
        self._sql = None
        self._customized_items = None
        self._tx_date = None
        self._job_name = None
        self._tx_time = None
        self._job_manager = None
        self._sql_buffer_index = None
        self._job_running_info = None
        self._session_id = None
        self._eq_type = None
        self._real_job_param = None
        self._init_vars()
        # 作业的并行度
        self._parallel_num = 3
        # 并行策略配置
        self._strategy_list = None
        # 离线兜底并行策略配置
        self._offline_strategy_list = None
        self.job_parallel = JobParallel()

    def _init_vars(self):
        assert len(sys.argv) > 1, 'one args at least'
        # 初始化作业参数解析器
        params = ' '.join(sys.argv)
        JobParam.init_parser(params)
        CONTROL_FILE = sys.argv[1]

        if is_tx_date_as_arg(CONTROL_FILE):
            tx_date = CONTROL_FILE
            tx_time = None
            job_name = "NO_JOB_NAME"
            job_manager = ''
            sql_buffer_index = '0'
            job_running_info = None
            eq_type = 'no_eq_type'
            session_id = '1'
        else:
            job_name, tx_date, tx_time = get_job_name_and_tx_date(CONTROL_FILE)
            job_manager = get_job_manager(job_name)
            sql_buffer_index = get_job_status_index(job_name, tx_date)
            job_running_info = get_job_running_info(job_name, tx_date)
            if job_running_info:
                eq_type = job_running_info['eqType'] if job_running_info else 'no_eq_type'
                session_id = job_running_info['sessionid'] if job_running_info else '1'
            else:
                eq_type = 'no_eq_type'
                session_id = '1'

        logging.info("JOB_NAME:" + job_name)
        logging.info("TX_DATE:" + tx_date)
        logging.info("EQ_TYPE:" + eq_type)
        logging.info("SESSION_ID:" + str(session_id))
        logging.info("SQL_BUFFER_INDEX:" + sql_buffer_index)
        # {yyyy}-{mm}-{dd}
        self._tx_date = tx_date
        self._job_name = job_name
        self._tx_time = tx_time
        self._job_manager = job_manager
        self._sql_buffer_index = sql_buffer_index
        self._session_id = session_id
        self._eq_type = eq_type
        # 初始化作业参数
        self._init_job_params()

    def _init_job_params(self):
        # 读取实时参数
        self._real_job_param = JobParam.sys_get(JobParamConst.REAL_TIME_PARAM_KEY)

    def set_sql_runner(self, sql_runner):
        self._sql_runner = sql_runner
        return self

    def set_customized_items(self, customized_items):
        self._customized_items = customized_items
        return self

    def set_parallel_num(self, parallel_num):
        self._parallel_num = parallel_num
        return self

    def get_customized_items(self):
        return self._customized_items

    def get_sql_runner(self):
        return self._sql_runner

    def add_export_env(self, export_env):
        self._export_list.add(export_env)
        return self

    def get_export_list(self):
        return self._export_list

    def add_cmdline_arg(self, cmdline_arg):
        self._cmdline_args.add(cmdline_arg)
        return self

    def get_cmdline_args(self):
        return self._cmdline_args

    def set_sql(self, sql):
        self._sql = sql
        return self

    def get_sql(self):
        return self._sql

    def get_tx_date(self):
        return self._tx_date

    def get_tx_time(self):
        return self._tx_time

    def get_job_name(self):
        return self._job_name

    def get_job_manager(self):
        return self._job_manager

    def get_sql_buffer_index(self):
        return self._sql_buffer_index

    def get_session_id(self):
        return self._session_id

    def get_eq_type(self):
        return self._eq_type

    def get_parallel_num(self):
        return self._parallel_num

    @property
    def real_time_use_offline(self):
        """
        实时是否使用了离线兜底
        """
        return str(self._real_job_param.get(JobParamConst.REAL_TIME_PARAM_USE_OFFLINE)) \
               == JobParamConst.REAL_TIME_PARAM_USE_OFFLINE_1 if self._real_job_param else False

    # This is deprecated
    def execute(self):
        logging.warn("execute() is deprecated, please download the latest template and use execute_sqls.")

        tx_date = self.get_tx_date()  # {yyyy}-{mm}-{dd}
        tx_time = self.get_tx_time()
        job_name = self.get_job_name()
        manager = self.get_job_manager()
        session_id = self.get_session_id()
        eq_type = self.get_eq_type()
        runner = self.get_sql_runner()

        time_dict = get_times(tx_date, tx_time)
        udf_dict = get_udf()
        global_dict = dict(time_dict.items() + udf_dict.items())

        # add password key
        global_dict['key'] = get_sql_password()

        mapreduce_name_template = "set mapreduce.job.name = {job_name}_{txDate};\n"

        logging.info("Begin Execute sql")
        mapreduce_name_stmt = mapreduce_name_template.format(job_name=job_name, txDate=tx_date.replace('-', ''))
        custom_args = "set custom.trace.name = {job_name};\n set custom.trace.owner.erp = {manager};\n " \
                      "set custom.trace.source = etl;\n set custom.trace.sessionid = {session_id};" \
            .format(job_name=job_name, manager=manager, session_id=session_id)
        sql = mapreduce_name_stmt + custom_args + self.get_sql().format(**global_dict)

        execute_engine = get_execute_engine(self.get_sql_runner())
        init_pre_settings(self.get_sql_runner(), execute_engine, self.get_export_list(), self.get_cmdline_args())

        execute_engine.set_compress(False)
        execute_engine.set_sql(sql)
        # execute_engine.set_silent(True)
        execute_engine.run()
        logging.info("=" * 100)
        logging.info("\n")

    def execute_sqls(self, sqls, offline_sqls=None):
        """
        sql执行
        sqls: 正常的SQL
        offline_sqls: 离线兜底的SQL, 如果不为None则检查是否兜底
        """
        if self.real_time_use_offline:
            if offline_sqls:
                logging.warn('warning: 实时数据不可用, 执行离线兜底SQL')
                # 离线兜底, 使用离线兜底SQL
                sqls = offline_sqls
            else:
                logging.warn('warning: 上游实时数据不可用, 但是未配置离线兜底SQL, 如有需求请配置离线SQL')

        tx_date = self.get_tx_date()  # {yyyy}-{mm}-{dd}
        tx_time = self.get_tx_time()
        job_name = self.get_job_name()
        manager = self.get_job_manager()
        sql_buffer_index = self.get_sql_buffer_index()
        session_id = self.get_session_id()
        eq_type = self.get_eq_type()
        runner = self.get_sql_runner()

        time_dict = get_times(tx_date, tx_time)
        udf_dict = get_udf()
        global_dict = dict(time_dict.items() + udf_dict.items() + self.get_customized_items().items())

        # add password key
        global_dict['key'] = get_sql_password()

        total_sqls = len(sqls)
        sorted_keys = sorted(sqls.keys())
        logging.info("SQL will be executed are:{0}".format(sorted_keys))

        for i, idx in enumerate(sorted_keys):
            if i < int(sql_buffer_index) and eq_type == 'retry':
                logging.info("current index is %s,  less than status_index %s, continue " % (i, sql_buffer_index))
                continue
            mapreduce_name_stmt = "set mapreduce.job.name = {job_name}_{tx_date}_{idx}/{total_sqls};\n" \
                .format(job_name=job_name, tx_date=tx_date.replace('-', ''), idx=idx, total_sqls=total_sqls)

            custom_args = "set custom.trace.name = {job_name};\n set custom.trace.owner.erp = {manager};\n " \
                          "set custom.trace.source = etl;\n set custom.trace.sessionid = {session_id};\n " \
                          "set custom.trace.bufferIndex = {idx};" \
                .format(job_name=job_name, manager=manager, session_id=session_id, idx=idx)

            logging.info(
                "=====Start to execute SQL：{idx}/{total_sqls}     =====".format(idx=idx, total_sqls=total_sqls))
            # 上报执行进度
            common_http_gravity.report_exe_process("Running", "Start", idx)
            sql = mapreduce_name_stmt + custom_args + sqls[idx].format(**global_dict)

            execute_engine = get_execute_engine(self.get_sql_runner())
            init_pre_settings(self.get_sql_runner(), execute_engine, self.get_export_list(), self.get_cmdline_args())

            execute_engine.set_compress(False)
            execute_engine.set_sql(sql)
            # execute_engine.set_silent(True)
            try:
                # TODO Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions.
                # TODO Consider using a different execution engine (i.e. spark, tez) or using Hive 1.X releases.
                if self.get_sql_runner() == RUNNER_SPARK_SQL:
                    execute_engine.run()
                elif self.get_sql_runner() == RUNNER_HIVE:
                    execute_engine.run()
                elif self.get_sql_runner() == RUNNER_STINGER:
                    # if stinger is the first choice,then it will try using hive after first failure.
                    execute_engine.run_with_retry()

                # 如果是最后一个buffer的话重置为开始的index
                if i + 1 == len(sorted_keys):
                    index = 0
                else:
                    index = i + 1
                if job_name != 'NO_JOB_NAME' and IS_MOCK != 'true' and update_job_status_buffer_index(job_name, tx_date, index) is False:
                    logging.warning("=====Failed to update sql buffer index：{idx}   =====".format(idx=idx))
            except Exception, e:
                logging.error(
                    "=====Failed to execute SQL：{idx}/{total_sqls}   =====".format(idx=idx, total_sqls=total_sqls))
                # 上报执行进度
                common_http_gravity.report_exe_process("Running", "Fail", idx)
                common_http_gravity.report_exe_process('Running', "Fail")
                raise e
            logging.info(
                "=====Successfully execute SQL：{idx}/{total_sqls} =====".format(idx=idx, total_sqls=total_sqls))
            # 上报执行进度
            common_http_gravity.report_exe_process("Running", "Success", idx)
        # 上报执行进度
        common_http_gravity.report_exe_process('Running', "Success")
        logging.info("{0} All SQLs completed {1}".format("=" * 30, "=" * 30))
        logging.info("\n")

    def execute_sqls_parallel(self, sqls, strategy_list, offline_sqls=None, offline_strategy_list=None):
        """
        sql执行
        sqls: 正常的SQL
        strategy_list: SQL并行策略
        offline_sqls: 离线兜底的SQL, 如果不为None则检查是否兜底
        offline_strategy_list: 离线兜底SQL的并行策略
        """
        if self.real_time_use_offline:
            if offline_sqls:
                logging.warn('warning: 实时数据不可用, 执行离线兜底SQL')
                # 离线兜底, 使用离线兜底SQL
                sqls = offline_sqls
                if offline_strategy_list is not None:
                    # 离线兜底的并行策略
                    strategy_list = offline_strategy_list
            else:
                logging.warn('warning: 上游实时数据不可用, 但是未配置离线兜底SQL, 如有需求请配置离线SQL')
        if self.get_parallel_num() > MAX_PARALLEL:
            logging.warn("parallel max number is {max_num}, current number is:{p_m}, will change parallel number to ${max_num}"
                         .format(p_m=self.get_parallel_num(), max_num=MAX_PARALLEL))
            self.set_parallel_num(MAX_PARALLEL)

        tx_date = self.get_tx_date()  # {yyyy}-{mm}-{dd}
        tx_time = self.get_tx_time()
        job_name = self.get_job_name()
        manager = self.get_job_manager()
        sql_buffer_index_str = self.get_sql_buffer_index()
        # 逗号分割的buffer index转成数组
        sql_buffer_index = [] if sql_buffer_index_str is None or sql_buffer_index_str == '' else sql_buffer_index_str.split(',')
        session_id = self.get_session_id()
        eq_type = self.get_eq_type()
        runner = self.get_sql_runner()

        time_dict = get_times(tx_date, tx_time)
        udf_dict = get_udf()
        global_dict = dict(time_dict.items() + udf_dict.items() + self.get_customized_items().items())

        # add password key
        global_dict['key'] = get_sql_password()

        total_sqls = len(sqls)
        sorted_keys = sorted(sqls.keys())
        logging.info("SQL will be executed are:{0}".format(sorted_keys))

        def send_buffer_index_fun(sql_keys):
            if job_name != 'NO_JOB_NAME' and IS_MOCK != 'true' and update_job_status_buffer_index(job_name, tx_date,
                                                                            ','.join(sql_keys)) is False:
                logging.warning("=====Failed to update sql buffer index：{sql_keys}   =====".format(sql_keys=sql_keys))

        def _execute_sql(sql_key, sql):
            # 当前SQL的层次
            current_sql_level = self.job_parallel.get_level(sql_key)
            # 上次作业执行时，已经执行过的sql的层次
            last_run_sql_level = self.job_parallel.get_sql_list_level(sql_buffer_index)
            if eq_type == 'retry' and (current_sql_level < last_run_sql_level or sql_key in sql_buffer_index):
                # 如果当前sql的层级小于上次执行过的sql的层级，则说明当前sql已经执行过了, 可以断点重跑
                # 如果当前sql在上次执行过的sql中，则说明当前sql已经执行过了，可以断点重跑
                logging.info("current index is %s,  less than status_index %s, continue " % (sql_key, sql_buffer_index_str))
                return
            mapreduce_name_stmt = "set mapreduce.job.name = {job_name}_{tx_date}_{idx}/{total_sqls};\n" \
                .format(job_name=job_name, tx_date=tx_date.replace('-', ''), idx=sql_key, total_sqls=total_sqls)

            custom_args = "set custom.trace.name = {job_name};\n set custom.trace.owner.erp = {manager};\n " \
                          "set custom.trace.source = etl;\n set custom.trace.sessionid = {session_id}; set custom.trace.bufferIndex = {idx}; " \
                .format(job_name=job_name, manager=manager, session_id=session_id, idx=sql_key)

            logging.info(
                "=====Start to execute SQL：{idx}/{total_sqls}     =====".format(idx=sql_key, total_sqls=total_sqls))

            sql = mapreduce_name_stmt + custom_args + sql.format(**global_dict)

            execute_engine = get_execute_engine(self.get_sql_runner())
            init_pre_settings(self.get_sql_runner(), execute_engine, self.get_export_list(), self.get_cmdline_args())

            execute_engine.set_compress(False)
            execute_engine.set_sql(sql)
            try:
                common_http_gravity.report_exe_process("Running", "Start", sql_key)
                # TODO Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions.
                # TODO Consider using a different execution engine (i.e. spark, tez) or using Hive 1.X releases.
                if self.get_sql_runner() == RUNNER_SPARK_SQL:
                    execute_engine.run()
                elif self.get_sql_runner() == RUNNER_HIVE:
                    execute_engine.run()
                elif self.get_sql_runner() == RUNNER_STINGER:
                    # if stinger is the first choice,then it will try using hive after first failure.
                    execute_engine.run_with_retry()
                self.job_parallel.handle_keys_with_lock(sql_key, send_buffer_index_fun)
            except Exception, e:
                common_http_gravity.report_exe_process("Running", "Fail", sql_key)
                logging.error(
                    "=====Failed to execute SQL：{idx}/{total_sqls}   =====".format(idx=sql_key, total_sqls=total_sqls))
                raise e

            common_http_gravity.report_exe_process('Running', "Success", sql_key)
            logging.info(
                "=====Successfully execute SQL：{idx}/{total_sqls} =====".format(idx=sql_key, total_sqls=total_sqls))
        logging.info("begin to execute SQL in parallel, parallel_num is: {parallel_num}".format(parallel_num=self.get_parallel_num()))

        self.job_parallel.run(_execute_sql, sqls, self.get_parallel_num(), strategy_list)
        self.job_parallel.handle_keys_with_lock('0', send_buffer_index_fun)
        logging.info("{0} All SQLs completed {1}".format("=" * 30, "=" * 30))
        logging.info("\n")


#  获取作业运行队列中的信息
def get_job_running_info(job_name, tx_date):
    json_dict = dict()
    json_dict['jobName'] = job_name
    json_dict['txDate'] = tx_date
    try:
        res = common_util.http_post_by_json(GRAVITY_URL + "/job/getRunningJobInfo", param_dict=json_dict)

        if res['success'] != 1:
            return None
        else:
            return res['result']

    except Exception, e:
        return None


def get_job_manager(job_name):
    json_dict = dict()
    json_dict['jobName'] = job_name
    try:
        res = common_util.http_post_by_json(CHANNEL_URL + "/channelsy/schedjob/getSchedJobInfoByJobName",
                                            param_dict=json_dict)

        if res['success'] != 1:
            return ""
        else:
            return res['result']['manager']

    except Exception, e:
        return ""


# 更新当前实例最新的index
def update_job_status_buffer_index(job_name, tx_date, sqlBufferIndex):
    json_dict = dict()
    json_dict['jobName'] = job_name
    json_dict['txDate'] = tx_date
    json_dict['sqlBufferIndex'] = sqlBufferIndex
    try:
        res = common_util.http_post_by_json(GRAVITY_URL + "/job/updateJobStatusSqlBufferIndex", param_dict=json_dict)

        return res['success'] == 1

    except Exception, e:
        return False


# 获取当前实例执行index
def get_job_status_index(job_name, tx_date):
    json_dict = dict()
    json_dict['jobName'] = job_name
    json_dict['txDate'] = tx_date
    try:
        res = common_util.http_post_by_json(GRAVITY_URL + "/job/getJobStatus", param_dict=json_dict)

        if res['success'] != 1:
            return "0"
        else:
            return res['result']['sqlBufferIndex']

    except Exception, e:
        return "0"


def is_tx_date_as_arg(input_string):
    if yyyy_mm_dd_pattern.match(input_string):
        return True


yyyy_mm_dd_pattern = re.compile(r"[\d]{4}-[\d]{2}-[\d]{2}")


def query_for_result(sql_runner, query_sql):
    """ In some case the job need to query db for a value """
    execute_engine = get_execute_engine(sql_runner)
    execute_engine.set_sql(query_sql)
    return execute_engine.fetch(True)
    