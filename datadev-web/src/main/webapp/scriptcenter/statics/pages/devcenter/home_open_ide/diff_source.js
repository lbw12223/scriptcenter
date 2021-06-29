import {createPatch} from 'diff';
import * as Diff2Html from 'diff2html';
import hljs from 'highlight.js';
import 'highlight.js/styles/googlecode.css';
import 'diff2html/bundles/css/diff2html.min.css';

export default class JmdCodeDiff {

    config = {
        oldString: '',
        newString: '',
        elId: '',
        context: 5,
        outputFormat: 'line-by-line',
        drawFileList: false,
        renderNothingWhenEmpty: false,
        fileName: false,
        isShowNoChange: false,
        showTitle: false,
        placeholder: '暂无数据',
        el: null,
        id: null
    };

    constructor(config) {
        if (!config || !config.el) {
            throw new Error("请传入dom节点")
        }
        this.mergeConfig(config)
        this.run();
    }

    mergeConfig(config) {
        for (let k in config) {
            // @ts-ignore
            this.config[k] = config[k]
        }
    }

    run() {
        this.insertDom(this.config.el)
    }

    // 生成Html字符串
    createdHtml(oldString, newString, context, outputFormat, drawFileList, renderNothingWhenEmpty, fileName, isShowNoChange) {
        function hljsSelf(html) {
            return html.replace(/<span class="d2h-code-line-ctn">(.+?)<\/span>/g, '<span class="d2h-code-line-ctn"><code>$1</code></span>');
        }

        if (isShowNoChange) {
            oldString = `${oldString}\no`;
            newString = `${newString}\nn`;
        }
        const args = [fileName, oldString, newString, '', '', {context}];
        const dd = createPatch(...args);
        const outStr = Diff2Html.parse(dd, {
            inputFormat: 'diff',
            outputFormat,
            drawFileList,
            matching: 'lines',
            renderNothingWhenEmpty,
        });
        const html = Diff2Html.html(outStr, {
            inputFormat: 'json',
            outputFormat,
            drawFileList,
            matching: 'lines',
            renderNothingWhenEmpty,
        });
        console.log(outStr, html)
        return hljsSelf(html);
    }

    // 高亮
    highlight(el) {
        const blocks = el.querySelectorAll('code');
        blocks.forEach((block) => {
            hljs.highlightBlock(block);
        });
    }

    // 插入html字符串
    insertDom(el) {
        const htmlStr = this.createdHtml(this.config.oldString, this.config.newString, this.config.context,
            this.config.outputFormat, this.config.drawFileList, this.config.renderNothingWhenEmpty,
            this.config.fileName, this.config.isShowNoChange);
        el.innerHTML = `<div class="code-diff-wrap" ${this.config.id ? `id=${this.config.id}` : ''}>${htmlStr}</div>`;
        this.highlight(el);
        this.delTopEl();
    }

    // 处理顶部dom元素
    delTopEl() {
        const trs = this.findFirstTd(0);

        if (trs && trs.length) {
            if (!this.config.oldString || this.config.oldString === 'null') {
                this._setEmptyWord(trs[1]);
            }
            this.delExtraLine(trs);
        }
        const trsNew = this.findFirstTd(1);
        console.log(trsNew,trs)
        if (trsNew && trsNew.length) {
            if (!this.config.newString || this.config.newString === 'null') {
                this._setEmptyWord(trsNew[1]);
            }
            this.delExtraLine(trsNew);
        }
    }

    // 查找第一个td
    findFirstTd(num) {
        const asides = document.querySelectorAll(`${this.config.elId} .d2h-files-diff .d2h-file-side-diff`);
        if (!asides) {
            return;
        }
        return asides[num].querySelectorAll('.d2h-diff-tbody tr');
    }

    // 设置空状态文字
    _setEmptyWord(el) {
        el && (el.innerHTML = `
       <td class="d2h-code-side-linenumber d2h-del d2h-change">
          1
       </td>
       <td class="d2h-del d2h-change">
            <div class="d2h-code-side-line d2h-del d2h-change">
                <span class="d2h-code-line-prefix">-</span>
                <span class="d2h-code-line-ctn"><code class="hljs"><del>${this.config.placeholder}</del></code></span>
            </div>
       </td>
      `);
    }

    // 删除第一行和第二行
    delExtraLine(trs) {
        const index = trs.length - 1;
        if (trs[index].parentNode) {
            trs[index].parentNode.removeChild(trs[index]);
        }
    }


    // 交换dom位置
    swapElements(a, b) {
        if (a === b) return;
        // 记录父元素
        const bp = b.parentNode;
        const ap = a.parentNode;
        // 记录下一个同级元素
        const an = a.nextElementSibling;
        const bn = b.nextElementSibling;
        // 如果参照物是邻近元素则直接调整位置
        if (an === b) return bp.insertBefore(b, a);
        if (bn === a) return ap.insertBefore(a, b);
        if (a.contains(b)) {
            return ap.insertBefore(b, a), bp.insertBefore(a, bn);
        } else {
            return bp.insertBefore(a, b), ap.insertBefore(b, an);
        }
    }

    update(config) {
        if (!config || !config.el) {
            throw new Error("请传入dom节点")
        }
        this.mergeConfig(config)
        this.run();
    }

    // 销毁
    destroy() {
        this.config.el.innerHTML = ''
    }

    // 是否有不同
    isEqual() {
        return this.config.oldString === this.config.newString;
    }
}
