import React from 'react'
import { render as rtlRender,screen } from '@testing-library/react'
import View from '../../../../components/Form/Item/View'
import '@testing-library/jest-dom/extend-expect';
import { Provider } from 'react-redux'
import { Router,Route } from 'react-router';
import { createMemoryHistory } from "history";
import configureStore from 'redux-mock-store';
import { mockstate } from './constatnts-edit';
import thunk from 'redux-thunk'


jest.mock('react-formio', () => ({
  ...jest.requireActual('react-formio'),
}));

const middlewares = [thunk] 
let store;
let mockStore = configureStore(middlewares);

beforeEach(()=>{
  store = mockStore(mockstate);
  store.dispatch = jest.fn();
})


function renderWithRouterMatch( ui,{ 
    path = "/", 
    route = "/", 
    history = createMemoryHistory({ initialEntries: [route] }),
  } = {}) { 
    return{ 
    ...rtlRender(  
        <Provider store={store}>
            <Router history={history}> 
                <Route path={path} component={ui}  /> 
            </Router>
          </Provider> )
      }
    
  }

it("should render the View component without breaking",async()=>{
    renderWithRouterMatch(View,{
        path:"/form/:formId",
        route:"/form/123",
    }
    )
  expect(screen.getByText("the form title")).toBeInTheDocument();
  expect(screen.getByText("Submit")).toBeInTheDocument();  
})
