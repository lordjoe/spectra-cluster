package uk.ac.ebi.pride.spectracluster.similarity;

import junit.framework.*;
import org.junit.*;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.similarity.TestSimilarityMethods
 *
 * @author Steve Lewis
 * @date 29/05/13
 */
public class TestSimilarityMethods {

    public static final String SPECTRUM1_MGF1 =
            "BEGIN IONS\n" +
                    "TITLE=1000\n" +
                    "PEPMASS=400.29999\n" +
                    "CHARGE=2.0+\n" +
                    "126.03780\t1.32\t1\n" +
                    "128.97778\t4.54\t1\n" +
                    "130.12135\t5.32\t1\n" +
                    "141.71326\t3.73\t1\n" +
                    "155.79489\t2.11\t1\n" +
                    "157.09422\t7.54\t1\n" +
                    "158.13786\t8.77\t1\n" +
                    "173.08929\t2.98\t1\n" +
                    "175.01099\t72.17\t1\n" +
                    "186.67210\t2.52\t1\n" +
                    "190.05768\t7.00\t1\n" +
                    "197.29556\t15.06\t1\n" +
                    "199.04955\t2.13\t1\n" +
                    "220.01772\t3.28\t1\n" +
                    "223.12512\t2.68\t1\n" +
                    "228.51855\t1.04\t1\n" +
                    "234.98022\t1.51\t1\n" +
                    "243.04980\t4.17\t1\n" +
                    "249.20453\t4.32\t1\n" +
                    "271.15723\t4.81\t1\n" +
                    "274.47568\t2.13\t1\n" +
                    "279.02142\t3.98\t1\n" +
                    "279.99423\t17.66\t1\n" +
                    "282.30859\t3.04\t1\n" +
                    "283.51581\t3.16\t1\n" +
                    "286.22876\t10.03\t1\n" +
                    "290.57080\t1.73\t1\n" +
                    "293.40524\t9.20\t1\n" +
                    "300.41602\t2.39\t1\n" +
                    "306.26575\t1.58\t1\n" +
                    "317.03546\t4.69\t1\n" +
                    "318.95529\t7.65\t1\n" +
                    "327.34570\t15.32\t1\n" +
                    "329.15344\t31.22\t1\n" +
                    "331.03186\t3.89\t1\n" +
                    "339.85724\t127.68\t1\n" +
                    "340.68414\t1.28\t1\n" +
                    "342.02014\t13.21\t1\n" +
                    "346.54468\t10.66\t1\n" +
                    "348.25540\t7.77\t1\n" +
                    "349.11072\t5.65\t1\n" +
                    "356.49588\t3.66\t1\n" +
                    "358.00571\t9.95\t1\n" +
                    "359.08218\t6.78\t1\n" +
                    "364.94586\t26.50\t1\n" +
                    "366.25629\t47.06\t1\n" +
                    "371.22272\t8.50\t1\n" +
                    "380.18915\t1.42\t1\n" +
                    "381.25684\t21.79\t1\n" +
                    "382.19437\t62.27\t1\n" +
                    "383.31369\t38.82\t1\n" +
                    "384.26367\t12.26\t1\n" +
                    "385.05157\t43.37\t1\n" +
                    "391.01205\t41.33\t1\n" +
                    "391.66846\t2.54\t1\n" +
                    "392.36270\t27.55\t1\n" +
                    "393.10971\t2.69\t1\n" +
                    "397.79718\t4.86\t1\n" +
                    "399.89346\t15.87\t1\n" +
                    "400.86118\t56.83\t1\n" +
                    "402.02863\t15.93\t1\n" +
                    "402.97165\t9.95\t1\n" +
                    "451.53363\t2.27\t1\n" +
                    "459.32065\t1.67\t1\n" +
                    "474.35086\t9.96\t1\n" +
                    "485.41226\t8.86\t1\n" +
                    "486.89313\t7.25\t1\n" +
                    "507.66586\t5.14\t1\n" +
                    "519.41101\t5.17\t1\n" +
                    "532.21863\t2.69\t1\n" +
                    "541.02563\t2.55\t1\n" +
                    "542.48981\t3.18\t1\n" +
                    "558.53662\t4.75\t1\n" +
                    "581.58582\t2.75\t1\n" +
                    "583.65271\t3.45\t1\n" +
                    "584.99292\t3.18\t1\n" +
                    "596.28265\t2.45\t1\n" +
                    "615.37720\t2.13\t1\n" +
                    "628.45862\t7.96\t1\n" +
                    "634.85840\t1.48\t1\n" +
                    "671.59094\t4.90\t1\n" +
                    "700.31726\t1.73\t1\n" +
                    "726.90869\t3.10\t1\n" +
                    "1064.92664\t3.94\t1\n" +
                    "1150.09473\t1.77\t1\n" +
                    "1347.98328\t1.92\t1\n" +
                    "1545.88293\t2.22\t1\n" +
                    "1607.71973\t2.67\t1\n" +
                    "END IONS\n";

    public static final String SPECTRUM1_MGF2 =
            "BEGIN IONS\n" +
                    "TITLE=2\n" +
                    "PEPMASS=400.29999\n" +
                    "CHARGE=2+\n" +
                    "126.03780\t1.77\t1\n" +
                    "128.97778\t6.10\t1\n" +
                    "130.12135\t7.15\t1\n" +
                    "141.71326\t5.02\t1\n" +
                    "155.79489\t2.84\t1\n" +
                    "157.09422\t10.14\t1\n" +
                    "158.13786\t11.79\t1\n" +
                    "173.08929\t4.01\t1\n" +
                    "175.01099\t97.09\t1\n" +
                    "186.67210\t3.39\t1\n" +
                    "190.05768\t9.41\t1\n" +
                    "197.29556\t20.27\t1\n" +
                    "199.04955\t2.87\t1\n" +
                    "220.01772\t4.41\t1\n" +
                    "223.12512\t3.61\t1\n" +
                    "228.51855\t1.40\t1\n" +
                    "234.98022\t2.03\t1\n" +
                    "243.04980\t5.61\t1\n" +
                    "249.20453\t5.81\t1\n" +
                    "271.15723\t6.47\t1\n" +
                    "274.47568\t2.87\t1\n" +
                    "279.02142\t5.35\t1\n" +
                    "279.99423\t23.76\t1\n" +
                    "282.30859\t4.09\t1\n" +
                    "283.51581\t4.25\t1\n" +
                    "286.22876\t13.49\t1\n" +
                    "290.57080\t2.33\t1\n" +
                    "293.40524\t12.38\t1\n" +
                    "300.41602\t3.21\t1\n" +
                    "306.26575\t2.13\t1\n" +
                    "317.03546\t6.32\t1\n" +
                    "318.95529\t10.29\t1\n" +
                    "327.34570\t20.61\t1\n" +
                    "329.15344\t42.00\t1\n" +
                    "331.03186\t5.23\t1\n" +
                    "339.85724\t171.77\t1\n" +
                    "340.68414\t1.72\t1\n" +
                    "342.02014\t17.77\t1\n" +
                    "346.54468\t14.34\t1\n" +
                    "348.25540\t10.45\t1\n" +
                    "349.11072\t7.60\t1\n" +
                    "356.49588\t4.92\t1\n" +
                    "358.00571\t13.38\t1\n" +
                    "359.08218\t9.13\t1\n" +
                    "364.94586\t35.65\t1\n" +
                    "366.25629\t63.31\t1\n" +
                    "371.22272\t11.44\t1\n" +
                    "380.18915\t1.91\t1\n" +
                    "381.25684\t29.32\t1\n" +
                    "382.19437\t83.78\t1\n" +
                    "383.31369\t52.23\t1\n" +
                    "384.26367\t16.49\t1\n" +
                    "385.05157\t58.35\t1\n" +
                    "391.01205\t55.61\t1\n" +
                    "391.66846\t3.42\t1\n" +
                    "392.36270\t37.07\t1\n" +
                    "393.10971\t3.63\t1\n" +
                    "397.79718\t6.54\t1\n" +
                    "399.89346\t21.36\t1\n" +
                    "400.86118\t76.45\t1\n" +
                    "402.02863\t21.43\t1\n" +
                    "402.97165\t13.39\t1\n" +
                    "451.53363\t3.05\t1\n" +
                    "459.32065\t2.25\t1\n" +
                    "474.35086\t13.40\t1\n" +
                    "485.41226\t11.92\t1\n" +
                    "486.89313\t9.76\t1\n" +
                    "507.66586\t6.92\t1\n" +
                    "519.41101\t6.95\t1\n" +
                    "532.21863\t3.62\t1\n" +
                    "541.02563\t3.44\t1\n" +
                    "542.48981\t4.27\t1\n" +
                    "558.53662\t6.40\t1\n" +
                    "581.58582\t3.70\t1\n" +
                    "583.65271\t4.65\t1\n" +
                    "584.99292\t4.28\t1\n" +
                    "596.28265\t3.30\t1\n" +
                    "615.37720\t2.86\t1\n" +
                    "628.45862\t10.71\t1\n" +
                    "634.85840\t1.99\t1\n" +
                    "671.59094\t6.60\t1\n" +
                    "700.31726\t2.33\t1\n" +
                    "726.90869\t4.17\t1\n" +
                    "1064.92664\t5.31\t1\n" +
                    "1150.09473\t2.38\t1\n" +
                    "1347.98328\t2.59\t1\n" +
                    "1545.88293\t2.98\t1\n" +
                    "1607.71973\t3.60\t1\n" +
                    "END IONS\n";


    public static final String Spectrum_String3 = "BEGIN IONS\n" +
            "CHARGE=1\n" +
            "PEPMASS=2074.005\n" +
            "TITLE=id=40457522,sequence=DIRDPMELLDEVENELK\n" +
            "60.0591 96.0\n" +
            "83.0612 192.0\n" +
            "114.0609 98.0\n" +
            "115.0884 217.0\n" +
            "126.0545 122.0\n" +
            "129.0664 429.0\n" +
            "147.1135 1548.0\n" +
            "169.0624 412.0\n" +
            "170.1097 103.0\n" +
            "173.129 151.0\n" +
            "175.1198 585.0\n" +
            "185.0934 797.0\n" +
            "187.0719 1020.0\n" +
            "189.0882 511.0\n" +
            "200.1032 272.0\n" +
            "210.0897 168.0\n" +
            "213.16 165.0\n" +
            "215.1444 498.0\n" +
            "217.0831 639.0\n" +
            "218.1491 239.0\n" +
            "223.0771 122.0\n" +
            "227.0659 111.0\n" +
            "228.1321 326.0\n" +
            "235.1091 172.0\n" +
            "253.0871 84.0\n" +
            "260.1985 145.0\n" +
            "276.1623 237.0\n" +
            "282.1899 99.0\n" +
            "283.1473 124.0\n" +
            "285.1642 162.0\n" +
            "289.1977 84.0\n" +
            "297.119 440.0\n" +
            "300.1229 1440.0\n" +
            "302.162 89.0\n" +
            "304.0972 208.0\n" +
            "312.156 607.0\n" +
            "313.1584 293.0\n" +
            "315.1751 99.0\n" +
            "320.1221 127.0\n" +
            "326.1595 341.0\n" +
            "331.1883 143.0\n" +
            "346.1292 161.0\n" +
            "354.1718 163.0\n" +
            "359.1853 298.0\n" +
            "359.2479 231.0\n" +
            "360.1449 300.0\n" +
            "365.1412 316.0\n" +
            "365.1672 313.0\n" +
            "369.1982 146.0\n" +
            "371.2301 117.0\n" +
            "381.1773 138.0\n" +
            "385.1618 89.0\n" +
            "394.1621 256.0\n" +
            "404.2073 302.0\n" +
            "405.2221 193.0\n" +
            "411.2403 135.0\n" +
            "413.1517 384.0\n" +
            "427.2037 159.0\n" +
            "428.2093 149.0\n" +
            "433.1811 133.0\n" +
            "438.2182 120.0\n" +
            "439.236 111.0\n" +
            "452.2048 397.0\n" +
            "453.2035 131.0\n" +
            "455.2267 361.0\n" +
            "457.2484 84.0\n" +
            "461.2562 1275.0\n" +
            "466.2089 151.0\n" +
            "471.2386 249.0\n" +
            "485.1646 172.0\n" +
            "490.2866 446.0\n" +
            "491.2272 112.0\n" +
            "497.2487 697.0\n" +
            "522.243 207.0\n" +
            "529.2398 276.0\n" +
            "530.7563 107.0\n" +
            "532.2639 1097.0\n" +
            "538.2766 170.0\n" +
            "543.3079 1034.0\n" +
            "545.3082 2892.0\n" +
            "546.2351 209.0\n" +
            "555.2968 280.0\n" +
            "562.2884 454.0\n" +
            "571.7692 137.0\n" +
            "585.2624 348.0\n" +
            "609.321 416.0\n" +
            "611.3279 225.0\n" +
            "615.7537 111.0\n" +
            "616.3441 1351.0\n" +
            "618.32 330.0\n" +
            "625.7875 143.0\n" +
            "626.7951 92.0\n" +
            "637.8012 89.0\n" +
            "640.8321 106.0\n" +
            "644.3061 237.0\n" +
            "651.7908 102.0\n" +
            "653.2908 96.0\n" +
            "654.3108 339.0\n" +
            "656.3242 607.0\n" +
            "659.8271 105.0\n" +
            "662.658 112.0\n" +
            "664.3549 317.0\n" +
            "670.8472 136.0\n" +
            "671.0236 84.0\n" +
            "671.3248 178.0\n" +
            "674.819 81.0\n" +
            "679.3203 98.0\n" +
            "681.3088 206.0\n" +
            "686.8102 120.0\n" +
            "696.3414 378.0\n" +
            "700.7977 106.0\n" +
            "712.3027 136.0\n" +
            "713.401 112.0\n" +
            "714.3828 2986.0\n" +
            "716.3698 278.0\n" +
            "731.3155 208.0\n" +
            "733.2904 294.0\n" +
            "738.3772 865.0\n" +
            "738.6946 489.0\n" +
            "738.8832 219.0\n" +
            "739.027 180.0\n" +
            "740.8756 103.0\n" +
            "745.3876 7491.0\n" +
            "747.3643 590.0\n" +
            "749.3476 165.0\n" +
            "749.725 100.0\n" +
            "750.4439 594.0\n" +
            "750.818 140.0\n" +
            "751.4669 201.0\n" +
            "754.8731 103.0\n" +
            "758.3651 401.0\n" +
            "758.6659 153.0\n" +
            "766.358 550.0\n" +
            "766.6903 335.0\n" +
            "767.0275 172.0\n" +
            "772.8495 274.0\n" +
            "783.672 80.0\n" +
            "785.369 1463.0\n" +
            "791.3674 792.0\n" +
            "793.3908 249.0\n" +
            "796.7087 162.0\n" +
            "809.9175 161.0\n" +
            "820.8432 193.0\n" +
            "829.4095 2176.0\n" +
            "830.3963 1650.0\n" +
            "842.391 493.0\n" +
            "843.363 810.0\n" +
            "851.4474 497.0\n" +
            "852.4027 235.0\n" +
            "859.4197 999.0\n" +
            "861.8192 93.0\n" +
            "862.443 466.0\n" +
            "862.8808 83.0\n" +
            "869.3816 228.0\n" +
            "869.8521 326.0\n" +
            "870.487 544.0\n" +
            "880.4522 683.0\n" +
            "894.7411 809.0\n" +
            "898.458 1760.0\n" +
            "899.8909 159.0\n" +
            "914.939 3975.0\n" +
            "924.7193 124.0\n" +
            "926.1795 162.0\n" +
            "927.3836 845.0\n" +
            "929.8143 131.0\n" +
            "931.4479 10209.0\n" +
            "936.9463 119.0\n" +
            "937.4582 705.0\n" +
            "941.918 372.0\n" +
            "942.476 336.0\n" +
            "947.94 313.0\n" +
            "950.9202 444.0\n" +
            "956.4269 2344.0\n" +
            "957.9254 122.0\n" +
            "961.9317 144.0\n" +
            "972.9452 296.0\n" +
            "973.453 2475.0\n" +
            "974.9896 179.0\n" +
            "975.4542 1533.0\n" +
            "983.9391 339.0\n" +
            "986.9728 188.0\n" +
            "998.4174 910.0\n" +
            "1011.481 342.0\n" +
            "1016.4079 282.0\n" +
            "1027.4919 840.0\n" +
            "1027.9365 221.0\n" +
            "1028.4987 987.0\n" +
            "1037.1854 210.0\n" +
            "1040.4766 383.0\n" +
            "1041.981 340.0\n" +
            "1042.165 142.0\n" +
            "1042.9886 183.0\n" +
            "1045.9813 167.0\n" +
            "1046.5231 528.0\n" +
            "1068.5095 1294.0\n" +
            "1074.9888 463.0\n" +
            "1083.4395 152.0\n" +
            "1087.5323 7638.0\n" +
            "1099.4314 324.0\n" +
            "1100.5408 287.0\n" +
            "1104.5267 391.0\n" +
            "1107.0292 906.0\n" +
            "1107.5168 483.0\n" +
            "1113.4952 382.0\n" +
            "1118.4904 1019.0\n" +
            "1123.9614 140.0\n" +
            "1127.5487 899.0\n" +
            "1131.5496 697.0\n" +
            "1132.548 1282.0\n" +
            "1139.5261 184.0\n" +
            "1140.0291 625.0\n" +
            "1140.5232 698.0\n" +
            "1141.5298 6377.0\n" +
            "1151.6165 580.0\n" +
            "1151.8655 189.0\n" +
            "1153.3326 293.0\n" +
            "1153.894 208.0\n" +
            "1155.558 2920.0\n" +
            "1156.9788 137.0\n" +
            "1159.0529 150.0\n" +
            "1171.5402 167.0\n" +
            "1176.0063 182.0\n" +
            "1176.5409 299.0\n" +
            "1202.5662 7678.0\n" +
            "1205.5535 663.0\n" +
            "1214.532 148.0\n" +
            "1215.5798 1618.0\n" +
            "1231.5629 996.0\n" +
            "1237.0149 101.0\n" +
            "1241.0927 94.0\n" +
            "1250.0598 111.0\n" +
            "1253.5721 5393.0\n" +
            "1256.0669 130.0\n" +
            "1256.5317 574.0\n" +
            "1259.6101 3448.0\n" +
            "1269.5928 1535.0\n" +
            "1275.5551 103.0\n" +
            "1279.6351 454.0\n" +
            "1284.5682 317.0\n" +
            "1297.6238 584.0\n" +
            "1302.624 778.0\n" +
            "1306.609 237.0\n" +
            "1308.6611 2248.0\n" +
            "1309.0413 105.0\n" +
            "1312.6335 197.0\n" +
            "1315.6545 9583.0\n" +
            "1326.1141 80.0\n" +
            "1326.594 394.0\n" +
            "1343.1287 264.0\n" +
            "1360.6226 1662.0\n" +
            "1362.0377 83.0\n" +
            "1362.6584 490.0\n" +
            "1364.6357 233.0\n" +
            "1367.6539 306.0\n" +
            "1386.0858 101.0\n" +
            "1389.7072 1086.0\n" +
            "1417.7083 283.0\n" +
            "1421.6217 395.0\n" +
            "1428.7576 636.0\n" +
            "1429.7301 806.0\n" +
            "1445.6759 813.0\n" +
            "1451.7155 121.0\n" +
            "1458.7091 220.0\n" +
            "1466.7205 462.0\n" +
            "1468.7275 631.0\n" +
            "1471.205 113.0\n" +
            "1472.7583 85.0\n" +
            "1477.7159 854.0\n" +
            "1482.6317 333.0\n" +
            "1489.7427 293.0\n" +
            "1511.7235 1020.0\n" +
            "1530.7952 401.0\n" +
            "1546.7244 835.0\n" +
            "1561.769 4088.0\n" +
            "1569.6758 238.0\n" +
            "1572.6801 276.0\n" +
            "1573.7855 142.0\n" +
            "1574.7351 88.0\n" +
            "1585.7236 164.0\n" +
            "1586.7561 212.0\n" +
            "1615.7784 956.0\n" +
            "1619.7437 92.0\n" +
            "1641.7467 211.0\n" +
            "1642.7004 165.0\n" +
            "1652.799 228.0\n" +
            "1653.434 99.0\n" +
            "1653.7708 378.0\n" +
            "1654.0889 169.0\n" +
            "1655.8365 85.0\n" +
            "1674.8538 2799.0\n" +
            "1676.8287 158.0\n" +
            "1681.7728 156.0\n" +
            "1683.7642 187.0\n" +
            "1700.8536 122.0\n" +
            "1704.7509 154.0\n" +
            "1706.788 481.0\n" +
            "1711.8331 280.0\n" +
            "1729.757 191.0\n" +
            "1731.7797 191.0\n" +
            "1749.7726 124.0\n" +
            "1766.8149 424.0\n" +
            "1784.7881 1006.0\n" +
            "1787.8414 217.0\n" +
            "1794.7979 216.0\n" +
            "1796.8472 90.0\n" +
            "1825.8507 306.0\n" +
            "1828.9182 468.0\n" +
            "1831.965 132.0\n" +
            "1846.9001 42869.0\n" +
            "1847.8962 599.0\n" +
            "1890.9353 2650.0\n" +
            "1895.8784 390.0\n" +
            "1899.9098 205.0\n" +
            "1900.853 145.0\n" +
            "1900.9989 161.0\n" +
            "1901.9084 318.0\n" +
            "1915.931 1827.0\n" +
            "1931.8568 131.0\n" +
            "1962.9318 11666.0\n" +
            "2056.9495 3067.0\n" +
            "2073.9944 29962.0\n" +
            "END IONS";


    private IPeptideSpectrumMatch spectrum1;
    private IPeptideSpectrumMatch spectrum2;
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private IPeptideSpectrumMatch spectrum3;

    @Before
    public void setUp() throws Exception {
        LineNumberReader inp = new LineNumberReader(new StringReader(SPECTRUM1_MGF1));
        spectrum1 = ParserUtilities.readMGFScan(inp);
        LineNumberReader inp2 = new LineNumberReader(new StringReader(SPECTRUM1_MGF2));
        spectrum2 = ParserUtilities.readMGFScan(inp2);

        LineNumberReader inp3 = new LineNumberReader(new StringReader(Spectrum_String3));
        spectrum3 = ParserUtilities.readMGFScan(inp3);

    }


     /**
     * make sure old and new tests give same similarity
     *
     * @throws Exception
     */
    @Test
    public void testSimilarity() throws Exception {
        SimilarityChecker oldSimilarity = new FrankEtAlDotProductOld();
        SimilarityChecker newSimilarity = new FrankEtAlDotProduct();

        double oldDP = oldSimilarity.assessSimilarity(spectrum1, spectrum2);
        double newDP = newSimilarity.assessSimilarity(spectrum1, spectrum2);

        Assert.assertEquals(oldDP, newDP, 0);

//        double newSimilarity = newSimilarity.assessSimilarity(spectrum1, spectrum2);
//
//        Assert.assertEquals(oldSimilarity,newSimilarity,0.001 * (oldSimilarity + newSimilarity));

    }


    @Test
    public void testSelfSimilarity() throws Exception {

        SimilarityChecker oldSimilarity = new FrankEtAlDotProductOld();
        SimilarityChecker newSimilarity = new FrankEtAlDotProduct();

        double toSelf = oldSimilarity.assessSimilarity(spectrum1, spectrum1);
        Assert.assertEquals(1, toSelf, 0);

        double toSelfNew = newSimilarity.assessSimilarity(spectrum1, spectrum1);
        Assert.assertEquals(1, toSelfNew, 0);


    }

    @Test
    public void testInterestingSpectra() throws Exception {
        List<IPeptideSpectrumMatch> spectra = ClusteringTestUtilities.readISpectraFromResource();

        SimilarityChecker oldSimilarity = new FrankEtAlDotProductOld();
        SimilarityChecker newSimilarity = new FrankEtAlDotProduct();

        for (IPeptideSpectrumMatch s1 : spectra) {
            for (IPeptideSpectrumMatch s2 : spectra) {
                if (s1.getId().equals("44905") && s2.getId().equals("67027"))            {
                    double oldDP = oldSimilarity.assessSimilarity(s1, s2);
                    double newDP = newSimilarity.assessSimilarity(s1, s2);

                    TestCase.assertEquals(String.format("Spec %s vs %s, DP org = %f, DP new = %f\n", s1.getId(), s2.getId(), oldDP, newDP), oldDP, newDP, 0.001);
                }
            }
        }
    }

    /**
     * test that FrankEtAlDotProductOld and  FrankEtAlDotProductJohannes
     * are the same
     * @throws Exception
     */
   // @Test
    public void testManySpectra() throws Exception {
        FrankEtAlDotProductJohannes.CHECK_BEST_PEAK_SPEC1 = false;

        List<IPeptideSpectrumMatch> spectra = ClusteringTestUtilities.readISpectraFromResource();

        SimilarityChecker oldSimilarity = new FrankEtAlDotProductOld();
        SimilarityChecker newSimilarity = new FrankEtAlDotProductJohannes();

        for (IPeptideSpectrumMatch s1 : spectra) {
            for (IPeptideSpectrumMatch s2 : spectra) {
                double oldDP = oldSimilarity.assessSimilarity(s1, s2);
                double newDP = newSimilarity.assessSimilarity(s1, s2);

                TestCase.assertEquals(String.format("Spec %s vs %s, DP org = %f, DP new = %f\n", s1.getId(), s2.getId(), oldDP, newDP), oldDP, newDP, 0.001);
            }
        }
    }
}
