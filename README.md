# Commonsense Causal Reasoning between Short Texts

## CausalNet

CausalNet consists of a large amount of causal relationships extracted from Bing web pages.

Each causal relationship is a triple as following:
CAUSE\_WORD[\t]EFFECT\_WORD[\t]FREQUENCY

You can download CausalNet from 
<http://adapt.seiee.sjtu.edu.cn/causal/>

If you have any questions, feel free to contact me at <jessherlock@sjtu.edu.cn> .

## Publications

Please cite the following paper if you are using CausalNet and the code. Thanks!

*   Zhiyi Luo, Yuchen Sha, Kenny Q. Zhu, Seung-won Hwang, Zhongyuan Wang, "**Commonsense Causal Reasonging between Short Texts**", 
Proc. of 15th Int. Conf. on Principles of Knowledge Representation and Reasonging (KR'2016), Cape Town, South Africa.

## Quick Start

This repository is an implementation of the approach proposed in 
"**Commonsense Causal Reasonging between Short Texts**", KR'2016.

Follow these steps to get started:

1. Download CausalNet from <http://adapt.seiee.sjtu.edu.cn/causal/> ,then `tar -xjf cs.tar.bz2`.

2. Download the KR-COPA.jar from <http://adapt.seiee.sjtu.edu.cn/causal/tools/KR-COPA.jar> .

3. Create the Log folder: `mkdir -p Log`

4. Set YOUR PATH in `light-copa-config.ini`

5. Run `java -Xmx25g -cp KR-COPA.jar edu.sjtu.copa.exe.COPAEvaluation`

