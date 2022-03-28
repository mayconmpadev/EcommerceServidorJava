package com.example.ecommerceservidorjava.util;

public class Validacao {
    static int verificador1;
    static int verificador2;
    static int verificador3;
    static int verificador4;
    static String cnpjTeste;

    public static boolean validarCpf(String cpf) {
        verificador1 = 0;
        verificador2 = 0;


        cpf = cpf.replaceAll("\\D", "");
        if (cpf.length()== 11){
            int num0 = Integer.parseInt(String.valueOf(cpf.charAt(0)));
            int num1 = Integer.parseInt(String.valueOf(cpf.charAt(1)));
            int num2 = Integer.parseInt(String.valueOf(cpf.charAt(2)));
            int num3 = Integer.parseInt(String.valueOf(cpf.charAt(3)));
            int num4 = Integer.parseInt(String.valueOf(cpf.charAt(4)));
            int num5 = Integer.parseInt(String.valueOf(cpf.charAt(5)));
            int num6 = Integer.parseInt(String.valueOf(cpf.charAt(6)));
            int num7 = Integer.parseInt(String.valueOf(cpf.charAt(7)));
            int num8 = Integer.parseInt(String.valueOf(cpf.charAt(8)));
            int num9 = Integer.parseInt(String.valueOf(cpf.charAt(9)));
            int num10 = Integer.parseInt(String.valueOf(cpf.charAt(10)));


            verificador1 = (num0 * 10) +
                    (num1 * 9) +
                    (num2 * 8) +
                    (num3 * 7) +
                    (num4 * 6) +
                    (num5 * 5) +
                    (num6 * 4) +
                    (num7 * 3) +
                    (num8 * 2);

            verificador2 = (num0 * 11) +
                    (num1 * 10) +
                    (num2 * 9) +
                    (num3 * 8) +
                    (num4 * 7) +
                    (num5 * 6) +
                    (num6 * 5) +
                    (num7 * 4) +
                    (num8 * 3) +
                    (num9 * 2);

            verificador1 = verificador1 % 11;
            if (verificador1 > 1) {
                verificador1 = 11 - verificador1;
            } else {
                verificador1 = 0;
            }

            verificador2 = verificador2 % 11;
            if (verificador2 > 1) {
                verificador2 = 11 - verificador2;
            } else {
                verificador2 = 0;
            }

            if (verificador1 == num9 && verificador2 == num10) {
                return true;
            } else {
                return false;
            }
        }else {
            return false;
        }

    }

    public static boolean validarCnpj(String cnpj) {
        verificador3 = 0;
        verificador4 = 0;

        cnpj = cnpj.replaceAll("\\D", "");

        if (cnpj.length() == 14){
            int num0 = Integer.parseInt(String.valueOf(cnpj.charAt(0)));
            int num1 = Integer.parseInt(String.valueOf(cnpj.charAt(1)));
            int num2 = Integer.parseInt(String.valueOf(cnpj.charAt(2)));
            int num3 = Integer.parseInt(String.valueOf(cnpj.charAt(3)));
            int num4 = Integer.parseInt(String.valueOf(cnpj.charAt(4)));
            int num5 = Integer.parseInt(String.valueOf(cnpj.charAt(5)));
            int num6 = Integer.parseInt(String.valueOf(cnpj.charAt(6)));
            int num7 = Integer.parseInt(String.valueOf(cnpj.charAt(7)));
            int num8 = Integer.parseInt(String.valueOf(cnpj.charAt(8)));
            int num9 = Integer.parseInt(String.valueOf(cnpj.charAt(9)));
            int num10 = Integer.parseInt(String.valueOf(cnpj.charAt(10)));
            int num11 = Integer.parseInt(String.valueOf(cnpj.charAt(11)));
            int num12 = Integer.parseInt(String.valueOf(cnpj.charAt(12)));
            int num13 = Integer.parseInt(String.valueOf(cnpj.charAt(13)));
            cnpjTeste = cnpj;

            verificador3 = (num0 * 5) +
                    (num1 * 4) +
                    (num2 * 3) +
                    (num3 * 2) +
                    (num4 * 9) +
                    (num5 * 8) +
                    (num6 * 7) +
                    (num7 * 6) +
                    (num8 * 5) +
                    (num9 * 4) +
                    (num10 * 3) +
                    (num11 * 2);

            verificador4 = (num0 * 6) +
                    (num1 * 5) +
                    (num2 * 4) +
                    (num3 * 3) +
                    (num4 * 2) +
                    (num5 * 9) +
                    (num6 * 8) +
                    (num7 * 7) +
                    (num8 * 6) +
                    (num9 * 5) +
                    (num10 * 4) +
                    (num11 * 3) +
                    (num12 * 2);

            verificador3 = verificador3 % 11;
            if (verificador3 > 1) {
                verificador3 = 11 - verificador3;
            } else {
                verificador3 = 0;
            }
            verificador4 = verificador4 % 11;
            if (verificador4 > 1) {
                verificador4 = 11 - verificador4;
            } else {
                verificador4 = 0;
            }
            if (verificador3 == num12 && verificador4 == num13) {
                return true;
            } else {
                return false;
            }
        }else {
            return false;
        }

    }

    public static boolean validarTelefone(String telefone){
        boolean a;
        if ((telefone.length() < 11) || (telefone.length() == 12)){ //27999405318 2733181538
            a = false;
        }else {
            a = true;
        }
        return a;
    }
}
