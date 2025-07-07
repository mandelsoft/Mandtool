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

/**
 *
 * @author Uwe Krueger
 */
public class Test {
  
  interface T1 {
    void F1();
  }
  
  interface T2 extends T1 {
    void F2();
  }
  
  interface Set<K,V> {
    void Set(K k,V v);
  }
  
  <K, V> V Add(Set<K,? super V> set, K k, V e){
    return e;
  }
  
  public void main() {
    Set<Integer,T1> set = null;
    
    T1 t1=null;
    T2 t2=null;
    
    t1 = Add(set, 2, t1);
    t2 = Add(set, 2, t2);
    
  }
}
