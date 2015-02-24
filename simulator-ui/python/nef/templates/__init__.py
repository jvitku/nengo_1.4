import integrator
import oscillator
import basalganglia
import basalganglia_rule
import linear_system
import binding
import networkarray
import gate
import learned_termination
import hpes_termination
import bcm_termination
import thalamus
import interneuron
import logic_crisp_gates_or
import logic_crisp_gates_xor
import logic_crisp_gates_nand
import logic_crisp_gates_and
import logic_crisp_gates_not
import logic_fuzzy_gates_not
import logic_fuzzy_gates_or
import logic_fuzzy_gates_and
import logic_fuzzy_member_linearDec
import logic_fuzzy_member_linearInc
import logic_fuzzy_member_triangular
import logic_fuzzy_member_trapezoid
import demoSubscriber
import demoPublisher
import vivae
import rl_mdp_sarsa

templates=[
    networkarray,
    integrator,
    oscillator,
    linear_system,
    binding,
    basalganglia,
    basalganglia_rule,
    thalamus,
    gate,
    learned_termination,
    interneuron,
    demoSubscriber,
    demoPublisher,
    logic_crisp_gates_or,
    logic_crisp_gates_xor,
    logic_crisp_gates_nand,
    logic_crisp_gates_and,
    logic_crisp_gates_not,
	logic_fuzzy_gates_not,
	logic_fuzzy_gates_or,
	logic_fuzzy_gates_and,
	logic_fuzzy_member_linearDec,# TODO: these two do not work in the GUI 
	logic_fuzzy_member_linearInc,
	logic_fuzzy_member_triangular,
	logic_fuzzy_member_trapezoid,
	vivae,
	rl_mdp_sarsa		
    ]
