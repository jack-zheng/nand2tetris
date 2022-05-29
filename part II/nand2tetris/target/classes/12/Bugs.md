# Bugs

1. screen 画直线太慢，据查可以使用 Output 中 16 bit 操作的方法加速
2. str = String.new(2); str.appendChar(); Output.printString(str); 这种方式打印文字会失败，打印数字没影响