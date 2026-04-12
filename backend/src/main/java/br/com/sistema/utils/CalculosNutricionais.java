package br.com.sistema.utils;

import br.com.sistema.enums.Sexo;

public class CalculosNutricionais {
    
    /**
     * Calcula o IMC (Índice de Massa Corporal)
     * @param peso em kg
     * @param altura em metros
     * @return IMC arredondado com 2 casas decimais
     */
    public static Double calcularIMC(Double peso, Double altura) {
        if (peso == null || altura == null || altura <= 0) {
            return null;
        }
        double imc = peso / (altura * altura);
        return Math.round(imc * 100.0) / 100.0;
    }
    
    /**
     * Calcula o percentual de gordura usando protocolo de 7 dobras (Jackson & Pollock)
     * @param sexo MASCULINO ou FEMININO
     * @param idade em anos
     * @param dobras array com as 7 dobras [triceps, peito, axilarMedia, subescapular, abdominal, supraIliaca, coxa]
     * @return Percentual de gordura arredondado com 2 casas decimais
     */
    public static Double calcularPercentualGordura(Sexo sexo, Integer idade, Double... dobras) {
        // Validar se todas as 7 dobras foram fornecidas
        if (dobras == null || dobras.length != 7) {
            return null;
        }
        
        // Verificar se alguma dobra é nula
        for (Double dobra : dobras) {
            if (dobra == null) {
                return null;
            }
        }
        
        if (idade == null || idade <= 0) {
            return null;
        }
        
        // Soma das 7 dobras
        double somaDobras = 0;
        for (Double dobra : dobras) {
            somaDobras += dobra;
        }
        
        double densidadeCorporal;
        
        if (sexo == Sexo.MASCULINO) {
            // Fórmula para homens (Jackson & Pollock, 1978)
            densidadeCorporal = 1.112 
                - (0.00043499 * somaDobras) 
                + (0.00000055 * Math.pow(somaDobras, 2)) 
                - (0.00028826 * idade);
        } else {
            // Fórmula para mulheres (Jackson & Pollock, 1980)
            densidadeCorporal = 1.097 
                - (0.00046971 * somaDobras) 
                + (0.00000056 * Math.pow(somaDobras, 2)) 
                - (0.00012828 * idade);
        }
        
        // Fórmula de Siri para conversão de densidade em %Gordura
        double percentualGordura = ((4.95 / densidadeCorporal) - 4.50) * 100;
        
        return Math.round(percentualGordura * 100.0) / 100.0;
    }
    
    /**
     * Calcula a massa gorda em kg
     * @param peso em kg
     * @param percentualGordura em %
     * @return Massa gorda arredondada com 2 casas decimais
     */
    public static Double calcularMassaGorda(Double peso, Double percentualGordura) {
        if (peso == null || percentualGordura == null) {
            return null;
        }
        double massaGorda = peso * (percentualGordura / 100.0);
        return Math.round(massaGorda * 100.0) / 100.0;
    }
    
    /**
     * Calcula a massa magra em kg
     * @param peso em kg
     * @param massaGorda em kg
     * @return Massa magra arredondada com 2 casas decimais
     */
    public static Double calcularMassaMagra(Double peso, Double massaGorda) {
        if (peso == null || massaGorda == null) {
            return null;
        }
        double massaMagra = peso - massaGorda;
        return Math.round(massaMagra * 100.0) / 100.0;
    }
    
    /**
     * Calcula a idade a partir da data de nascimento
     * @param dataNascimento
     * @return idade em anos
     */
    public static Integer calcularIdade(java.time.LocalDate dataNascimento) {
        if (dataNascimento == null) {
            return null;
        }
        return java.time.Period.between(dataNascimento, java.time.LocalDate.now()).getYears();
    }
}