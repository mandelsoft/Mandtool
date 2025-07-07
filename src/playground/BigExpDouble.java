/*
 * Copyright 2022 Uwe Krueger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playground;

import com.mandelsoft.mand.util.MandArith;
import java.lang.Math;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 *
 * @author Uwe Krueger
 */
public class BigExpDouble {

  double mantisse;
  int exponent;

  public BigExpDouble(double m, int e)
  {
    int exp = Math.getExponent(m);
    mantisse = Math.scalb(m, -exp);
    exponent = e + exp;
  }

  public BigExpDouble(double m)
  {
    int e = Math.getExponent(m);

    mantisse = Math.scalb(m, -Math.getExponent(m));
    exponent = e;
  }

  public BigExpDouble(BigDecimal b)
  {
    b = b.stripTrailingZeros();
    String s = b.unscaledValue().toString();
    int exp = b.scale() - s.length();
    double f = Math.log(10) / Math.log(2) * (-exp);
    int e = (int) Math.floor(f);
    double m = b.scaleByPowerOfTen(exp).doubleValue() * Math.pow(2, f - e);
    mantisse = m;
    exponent = e;
  }

  public String toString()
  {
    BigDecimal m = new BigDecimal(mantisse);

    m = MandArith.mul(m, MandArith.b2.pow(exponent, MandArith.MC));
    return m.round(new MathContext(16)).toString();
  }

  public double toDouble()
  {
    return mantisse * Math.pow(2, exponent);
  }

  public BigExpDouble mul(BigExpDouble v)
  {
    double m = this.mantisse * v.mantisse;
    int e = this.exponent + v.exponent;

    return new BigExpDouble(m, e);
  }

  public BigExpDouble div(BigExpDouble v)
  {
    double m = this.mantisse / v.mantisse;
    int e = this.exponent - v.exponent;

    return new BigExpDouble(m, e);
  }

  public BigExpDouble add(BigExpDouble v)
  {
    double m;
    int e;
    if (exponent >= v.exponent) {
      m = mantisse + Math.scalb(v.mantisse, v.exponent - exponent);
      e = exponent;
    }
    else {
      m = v.mantisse + Math.scalb(mantisse, exponent - v.exponent);
      e = v.exponent;
    }

    return new BigExpDouble(m, e);
  }

  public BigExpDouble sub(BigExpDouble v)
  {
    double m;
    int e;
    if (exponent >= v.exponent) {
      m = mantisse - Math.scalb(v.mantisse, v.exponent - exponent);
      e = exponent;
    }
    else {
      m = Math.scalb(mantisse, exponent - v.exponent) - v.mantisse;
      e = v.exponent;
    }

    return new BigExpDouble(m, e);
  }

  public boolean lt(BigExpDouble v)
  {
    if (exponent == v.exponent) {
      return mantisse < v.mantisse;
    }
    return exponent < v.exponent;
  }

  public boolean gt(BigExpDouble v)
  {
    if (exponent == v.exponent) {
      return mantisse > v.mantisse;
    }
    return exponent > v.exponent;
  }

  public static void test(double d)
  {

    System.out.printf("%g: %d %g\n", d, Math.getExponent(d), Math.scalb(d, -Math.getExponent(d)));

  }

  public static void math(double a, double b)
  {
    BigExpDouble ba = new BigExpDouble(a);
    BigExpDouble bb = new BigExpDouble(b);

    System.out.printf("%g * %g = %g\n", a, b, a * b);
    System.out.printf("%s * %s = %s\n", ba, bb, ba.mul(bb));
    System.out.printf("%g / %g = %g\n", a, b, a / b);
    System.out.printf("%s / %s = %s\n", ba, bb, ba.div(bb));
    System.out.printf("%g + %g = %g\n", a, b, a + b);
    System.out.printf("%s + %s = %s\n", ba, bb, ba.add(bb));
    System.out.printf("%g - %g = %g\n", a, b, a - b);
    System.out.printf("%s - %s = %s\n", ba, bb, ba.sub(bb));
  }

  public static void convert(double a)
  {
    BigDecimal b = new BigDecimal(a, MandArith.MC);
    System.out.printf("%g: %s (%d) %s\n", a, b, b.scale(), b.unscaledValue());
    BigExpDouble bd = new BigExpDouble(b);
    System.out.printf("    %s\n", bd);
  }

  public static void convert(BigDecimal b)
  {
    System.out.printf("%s:(%d) %s\n", b, b.scale(), b.unscaledValue());
    BigExpDouble bd = new BigExpDouble(b);
    System.out.printf("    %s\n", bd);
  }

  public static void main(String[] args)
  {
    System.out.println("test");
    test(1.0);
    test(2.0);
    test(4.0);
    test(8.0);
    test(10.0);
    test(64.0);
    test(64.5);
    test(100.0);
    test(1.234);
    test(147.264534);
    test(1e308);

    math(8, 2);
    math(2, 8);
    math(10, 100);
    math(3.145, 100);
    math(1e308, 1e308);

    convert(64);
    convert(0.25);
    convert(1e10);
    convert(1e300);
    convert(-20);
    convert(-0.125);
    convert(new BigDecimal(1e300, MandArith.MC).multiply(new BigDecimal(1e300, MandArith.MC)));
  }
}
