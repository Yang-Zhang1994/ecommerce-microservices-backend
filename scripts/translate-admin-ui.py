#!/usr/bin/env python3
"""Translate remaining Chinese admin UI strings in renren-fast-vue (buttons, labels, validation)."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VIEWS = ROOT / "renren-fast-vue" / "src" / "views"

# Order matters: longer phrases first
REPLACEMENTS = [
    ("批量删除", "Batch Delete"),
    ("批量上架", "Batch On Sale"),
    ("批量下架", "Batch Off Sale"),
    ("确定进行", "Confirm "),
    ("确定对", "Confirm "),
    ("操作?", " this action?"),
    ("操作？", " this action?"),
    ("不能为空", " is required"),
    ("查询", "Query"),
    ("新增", "Add"),
    ("修改", "Edit"),
    ("删除", "Delete"),
    ("操作", "Actions"),
    ("参数名", "Param Key"),
    ("参数值", "Param Value"),
    ("备注", "Remark"),
    ("提示", "Tip"),
    ("确定", "Confirm"),
    ("取消", "Cancel"),
    ("上传", "Upload"),
    ("云存储配置", "Cloud Storage Config"),
    ("云存储", "Cloud Storage"),
    ("本地存储", "Local Storage"),
    ("七牛", "Qiniu"),
    ("阿里云", "Aliyun"),
    ("腾讯云", "Tencent Cloud"),
    ("任务日志", "Job Log"),
    ("日志列表", "Log List"),
    ("暂停", "Pause"),
    ("恢复", "Resume"),
    ("立即执行", "Run Now"),
    ("用户名", "Username"),
    ("密码", "Password"),
    ("邮箱", "Email"),
    ("手机号", "Mobile"),
    ("角色", "Role"),
    ("状态", "Status"),
    ("创建时间", "Created At"),
    ("更新时间", "Updated At"),
    ("名称", "Name"),
    ("描述", "Description"),
    ("类型", "Type"),
    ("排序", "Sort"),
    ("启用", "Enable"),
    ("禁用", "Disable"),
    ("未知", "Unknown"),
    ("保存", "Save"),
    ("提交", "Submit"),
    ("返回", "Back"),
    ("关闭", "Close"),
    ("搜索", "Search"),
    ("重置", "Reset"),
    ("详情", "Details"),
    ("查看", "View"),
    ("下载", "Download"),
    ("刷新", "Refresh"),
    ("请选择", "Please select"),
    ("请输入", "Please enter"),
    ("会员等级id", "Member level ID"),
    ("会员等级", "Member Level"),
    ("用户名", "Username"),
    ("昵称", "Nickname"),
    ("头像", "Avatar"),
    ("性别", "Gender"),
    ("生日", "Birthday"),
    ("所在城市", "City"),
    ("职业", "Occupation"),
    ("个性签名", "Signature"),
    ("用户来源", "User Source"),
    ("积分", "Points"),
    ("成长值", "Growth Value"),
    ("启用状态", "Status"),
    ("注册时间", "Registration Time"),
    ("采购人id", "Purchaser ID"),
    ("采购人名", "Purchaser Name"),
    ("联系方式", "Contact"),
    ("仓库id", "Warehouse ID"),
    ("总金额", "Total Amount"),
    ("创建日期", "Created Date"),
    ("更新日期", "Updated Date"),
    ("sku_id", "SKU ID"),
    ("sku_name", "SKU Name"),
    ("购买个数", "Quantity"),
    ("工作单id", "Task ID"),
    ("弹窗, 新增 / 修改", "Add / Edit dialog"),
    ("获取数据列表", "Load data list"),
    ("每页数", "Page size"),
    ("当前页", "Current page"),
    ("多选", "Multi-select"),
]

# Fix awkward fragments from partial replacements
POST_FIXES = [
    ("Confirm [", "Are you sure you want to ["),
    (" is required is required", " is required"),
    ("Queryery", "Query"),
    ("Adddd", "Add"),
    ("批量Pause", "Batch Pause"),
    ("批量Resume", "Batch Resume"),
    ("批量Run Now", "Batch Run Now"),
    ("Upload文件", "Upload File"),
    ("Mobile码", "Mobile"),
    ("YesNo叠加其他优惠", "Stackable with other offers"),
    ("YesNo叠加优惠", "Stackable with other offers"),
    ("存储Type", "Storage Type"),
    ("拼命加载中", "Loading..."),
    ("一级菜单", "Top-level menu"),
    ("(未命名)", "(Unnamed)"),
    ("正常", "Normal"),
    ("成功", "Success"),
    ("失败", "Failed"),
    ("参数", "Parameters"),
    ("cron表达式", "Cron expression"),
    ("任务ID", "Job ID"),
    ("日志ID", "Log ID"),
    ("耗时(单位: 毫秒)", "Duration (ms)"),
    ("执行时间", "Executed at"),
    ("满几件", "Min quantity"),
    ("打几折", "Discount"),
    ("折后价", "Discounted price"),
    ("不可叠加", "Not stackable"),
    ("可叠加", "Stackable"),
    ("场次id", "Session ID"),
    ("活动场次id", "Promotion session ID"),
    ("商品id", "SKU ID"),
    ("秒杀价格", "Seckill price"),
    ("秒杀总量", "Seckill stock"),
    ("每人限购数量", "Limit per user"),
    ("订阅时间", "Subscribed at"),
    ("发送时间", "Sent at"),
    ("通知方式[0-短信，1-邮件]", "Notify via (0=SMS, 1=Email)"),
    ("URL地址", "URL"),
    ("将文件拖到此处，或", "Drop file here, or "),
    ("点击Upload", "click to upload"),
    ("只支持jpg、png、gif格式的图片！", "JPG, PNG and GIF only."),
    ("路径前缀", "Path prefix"),
    ("空间名", "Bucket name"),
    ("域名", "Domain"),
    ("Bucket所属地区", "Bucket region"),
    ("不设置默认为空", "Leave blank for default"),
    ("免费申请(Qiniu)10GB储存空间", "Free Qiniu 10GB storage"),
    ("Qiniu绑定的域名", "Qiniu domain"),
    ("Qiniu存储空间名", "Qiniu bucket"),
    ("Aliyun绑定的域名", "Aliyun domain"),
    ("Tencent Cloud绑定的域名", "Tencent Cloud domain"),
    ("如：sh（可选值 ，华南：gz 华北：tj 华东：sh）", "e.g. sh (gz/tj/sh)"),
    ("新建", "New"),
    ("已分配", "Assigned"),
    ("正在采购", "In progress"),
    ("已完成", "Completed"),
    ("采购失败", "Purchase failed"),
    ("// 表单Submit", "// form submit"),
    ("采购Failed", "Purchase failed"),
    ("绑定的Domain", "bound domain"),
    ("存储Bucket name", "bucket name"),
    ("活动Session ID", "Promotion session ID"),
    ("场次Name", "Session name"),
    ("每日开始时间", "Daily start time"),
    ("每日结束时间", "Daily end time"),
    ("活动标题", "Promotion title"),
    ("生效日期", "Effective dates"),
    ("开始日期", "Start date"),
    ("结束日期", "End date"),
    ("上下线Status", "Online status"),
    ("上线", "Online"),
    ("下线", "Offline"),
    ("创建人", "Created by"),
    ("通知方式", "Notification method"),
    ("短信", "SMS"),
    ("邮件", "Email"),
    ("专题名字", "Subject name"),
    ("专题id", "Subject ID"),
    ("专题标题", "Subject title"),
    ("专题副标题", "Subject subtitle"),
    ("显示Status", "Display status"),
    ("Details连接", "Detail URL"),
    ("专题图片地址", "Subject image URL"),
    ("名字", "Name"),
    ("图片地址", "Image URL"),
    ("开始时间", "Start time"),
    ("结束时间", "End time"),
    ("点击数", "Click count"),
    ("广告Details连接地址", "Ad detail URL"),
    ("发布者", "Publisher"),
    ("如: testTask", "e.g. testTask"),
    ("如: 0 0 12 * * ?", "e.g. 0 0 12 * * ?"),
    ("广告Detail URL地址", "Ad detail URL"),
    ("审核者", "Reviewer"),
    ("优惠卷Type", "Coupon type"),
    ("优惠卷Name", "Coupon name"),
    ("优惠券图片", "Coupon image"),
    ("数量", "Quantity"),
    ("金额", "Amount"),
    ("每人限领张数", "Limit per person"),
    ("使用门槛（最小Points）", "Min points to use"),
    ("有效时间", "Valid period"),
    ("使用Type", "Usage type"),
    ("全场通用", "Site-wide"),
    ("指定分类", "Specific category"),
    ("指定商品", "Specific product"),
    ("发行数量", "Publish count"),
    ("领取日期", "Claim period"),
    ("优惠码", "Promo code"),
    ("领取所需等级", "Required member level"),
    ("不限制", "No limit"),
    ("全场赠券", "Site-wide gift"),
    ("会员赠券", "Member gift"),
    ("购物赠券", "Purchase gift"),
    ("注册赠券", "Registration gift"),
    ("优惠券id", "Coupon ID"),
    ("产品分类id", "Category ID"),
    ("产品分类Name", "Category name"),
]

VALIDATION_FIX = re.compile(
    r"message:\s*['\"]([^'\"]*?) is required['\"]",
    re.UNICODE,
)


def normalize_validation(text: str) -> str:
    def fix_field(m: re.Match) -> str:
        field = m.group(1).strip()
        # If still Chinese-heavy, use generic
        if re.search(r"[\u4e00-\u9fff]", field):
            return m.group(0).replace(field + " is required", "This field is required")
        # camelCase / snake -> Title
        label = field.replace("_", " ").strip()
        if label and label[0].islower():
            label = label[0].upper() + label[1:]
        return f"message: '{label} is required'"

    return VALIDATION_FIX.sub(fix_field, text)


def process_file(path: Path) -> bool:
    try:
        content = path.read_text(encoding="utf-8")
    except OSError:
        return False
    if not re.search(r"[\u4e00-\u9fff]", content):
        return False
    original = content
    for old, new in REPLACEMENTS:
        content = content.replace(old, new)
    for old, new in POST_FIXES:
        content = content.replace(old, new)
    content = normalize_validation(content)
    if content != original:
        path.write_text(content, encoding="utf-8")
        return True
    return False


def main() -> None:
    changed = []
    for path in sorted(VIEWS.rglob("*.vue")):
        if process_file(path):
            changed.append(path.relative_to(ROOT))
    print(f"Updated {len(changed)} vue files under views/")
    for p in changed[:20]:
        print(f"  - {p}")
    if len(changed) > 20:
        print(f"  ... and {len(changed) - 20} more")


if __name__ == "__main__":
    main()
